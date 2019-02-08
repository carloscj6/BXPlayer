package com.revosleap.bxplayer.utils.playback

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.PowerManager
import android.support.v4.media.session.MediaSessionCompat
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.google.gson.Gson
import com.revosleap.bxplayer.callbacks.PlayerAdapter
import com.revosleap.bxplayer.models.Album
import com.revosleap.bxplayer.models.Song
import com.revosleap.bxplayer.services.MusicPlayerService
import com.revosleap.bxplayer.utils.utils.EqualizerUtils
import com.revosleap.bxplayer.utils.utils.PreferenceHelper
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


class MediaPlayerHolder internal constructor(private val mMusicService: MusicPlayerService?)
    : PlayerAdapter, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {
    // we have full audio focus
    private var ongoingCall: Boolean = false
    private val context: Context = mMusicService!!.applicationContext
    private val audioManager: AudioManager
    private var mediaPlayer: MediaPlayer? = null
    private var playbackInfoListener: PlaybackInfoListener? = null
    private var scheduledExecutorService: ScheduledExecutorService? = null
    private var seekBarPositionUpdateTask: Runnable? = null
    private var playingSong: Song? = null
    private var songs: MutableList<Song>? = null
    private var playingAlbum: Album? = null
    private var isReplaySong = false
    @PlaybackInfoListener.State
    private var state: Int = 0
    private val mediaSession: MediaSessionCompat? = null
    private var notificationActionsReceiver: NotificationReceiver? = null
    private var musicNotificationManager: BXNotificationManager? = null
    private var currentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK
    private var playOnFocusGain: Boolean = false
    private var phoneStateListener: PhoneStateListener? = null
    private var telephonyManager: TelephonyManager? = null
    private val preferenceHelper = PreferenceHelper(context)
    private val onAudioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> currentAudioFocusState = AUDIO_FOCUSED
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->
                // Audio focus was lost, but it's possible to duck (i.e.: play quietly)
                currentAudioFocusState = AUDIO_NO_FOCUS_CAN_DUCK
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Lost audio focus, but will gain it back (shortly), so note whether
                // playback should resume
                currentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK
                playOnFocusGain = isMediaPlayer() && state == PlaybackInfoListener.State.PLAYING || state == PlaybackInfoListener.State.RESUMED
            }
            AudioManager.AUDIOFOCUS_LOSS ->
                // Lost audio focus, probably "permanently"
                currentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK
        }

        if (mediaPlayer != null) {
            // Update the player state based on the change
            configurePlayerState()
        }
    }
    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            pauseMediaPlayer()
        }
    }

    init {
        audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager
    }

    private fun registerActionsReceiver() {
        notificationActionsReceiver = NotificationReceiver()
        val intentFilter = IntentFilter()

        intentFilter.addAction(BXNotificationManager.PREV_ACTION)
        intentFilter.addAction(BXNotificationManager.PLAY_PAUSE_ACTION)
        intentFilter.addAction(BXNotificationManager.NEXT_ACTION)
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG)
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)

        mMusicService!!.registerReceiver(notificationActionsReceiver, intentFilter)
    }

    private fun unregisterActionsReceiver() {
        if (mMusicService != null && notificationActionsReceiver != null) {
            try {
                mMusicService.unregisterReceiver(notificationActionsReceiver)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }

        }
    }

    override fun registerNotificationActionsReceiver(isRegister: Boolean) {

        if (isRegister) {
            registerActionsReceiver()
        } else {
            unregisterActionsReceiver()
        }
    }


    override fun getCurrentSong(): Song? {
        val index = songs?.indexOf(playingSong)
        preferenceHelper.currentIndex = index!!
        return playingSong
    }

    override fun getCurrentSongs(): MutableList<Song>? {
        return songs
    }

    override fun getSelectedAlbum(): Album? {
        return playingAlbum
    }

    override fun setSelectedAlbum(album: Album) {
        playingAlbum = album
    }

    override fun setCurrentSong(song: Song, songs: MutableList<Song>) {
        playingSong = song
        this.songs = songs
        if (songs.size > 0) {
            saveSongs(songs)
        }
    }

    override fun shufflePlayList() {
        songs?.shuffle()
        saveSongs(songs!!)
    }

    private fun saveSongs(songs: MutableList<Song>) {
        val gson = Gson()
        val gsonString = gson.toJson(songs)
        preferenceHelper.playingList = gsonString
    }

    override fun onCompletion(mediaPlayer: MediaPlayer) {
        if (playbackInfoListener != null) {
            playbackInfoListener!!.onStateChanged(PlaybackInfoListener.State.COMPLETED)
            playbackInfoListener!!.onPlaybackCompleted()
        }

        if (isReplaySong) {
            if (isMediaPlayer()) {
                resetSong()
            }
            isReplaySong = false
        } else {
            skip(true)
        }
    }

    override fun onResumeActivity() {
        startUpdatingCallbackWithPosition()
    }

    override fun onPauseActivity() {
        stopUpdatingCallbackWithPosition()
    }

    private fun tryToGetAudioFocus() {

        val result = audioManager.requestAudioFocus(
                onAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            currentAudioFocusState = AUDIO_FOCUSED
        } else {
            currentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK
        }
    }

    private fun giveUpAudioFocus() {
        if (audioManager.abandonAudioFocus(onAudioFocusChangeListener) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            currentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK
        }
    }

    override fun setPlaybackInfoListener(playbackInfoListener: PlaybackInfoListener) {
        this.playbackInfoListener = playbackInfoListener
    }

    private fun setStatus(@PlaybackInfoListener.State state: Int) {

        this.state = state
        if (playbackInfoListener != null) {
            playbackInfoListener!!.onStateChanged(state)
        }
    }

    private fun resumeMediaPlayer() {
        if (!isPlaying()) {
            mediaPlayer!!.start()
            setStatus(PlaybackInfoListener.State.RESUMED)
            mMusicService!!.startForeground(BXNotificationManager.NOTIFICATION_ID,
                    musicNotificationManager!!.createNotification())
        }
    }

    fun pauseMediaPlayer() {
        setStatus(PlaybackInfoListener.State.PAUSED)
        mediaPlayer!!.pause()
        mMusicService!!.stopForeground(false)
        musicNotificationManager!!.notificationManager.notify(BXNotificationManager.NOTIFICATION_ID,
                musicNotificationManager!!.createNotification())
    }

    private fun resetSong() {
        mediaPlayer!!.seekTo(0)
        mediaPlayer!!.start()
        setStatus(PlaybackInfoListener.State.PLAYING)
    }

    /**
     * Syncs the mediaPlayer position with mPlaybackProgressCallback via recurring task.
     */
    private fun startUpdatingCallbackWithPosition() {
        if (scheduledExecutorService == null) {
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
        }
        if (seekBarPositionUpdateTask == null) {
            seekBarPositionUpdateTask = Runnable { updateProgressCallbackTask() }
        }

        scheduledExecutorService!!.scheduleAtFixedRate(
                seekBarPositionUpdateTask,
                0,
                1000,
                TimeUnit.MILLISECONDS
        )
    }

    // Reports media playback position to mPlaybackProgressCallback.
    private fun stopUpdatingCallbackWithPosition() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService!!.shutdownNow()
            scheduledExecutorService = null
            seekBarPositionUpdateTask = null
        }
    }

    private fun updateProgressCallbackTask() {
        if (isMediaPlayer() && mediaPlayer!!.isPlaying) {
            val currentPosition = mediaPlayer!!.currentPosition
            if (playbackInfoListener != null) {
                playbackInfoListener!!.onPositionChanged(currentPosition)
            }
        }
    }

    override fun instantReset() {
        if (isMediaPlayer()) {
            if (mediaPlayer!!.currentPosition < 5000) {
                skip(false)
            } else {
                resetSong()
            }
        }
    }


    override fun initMediaPlayer() {

        try {
            if (mediaPlayer != null) {
                mediaPlayer!!.reset()
            } else {
                mediaPlayer = MediaPlayer()
                EqualizerUtils.openAudioEffectSession(context, mediaPlayer!!.audioSessionId)

                mediaPlayer!!.setOnPreparedListener(this)
                mediaPlayer!!.setOnCompletionListener(this)
                mediaPlayer!!.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
                mediaPlayer!!.setAudioAttributes(AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                musicNotificationManager = mMusicService!!.musicNotificationManager
                callStateListener()

            }
            tryToGetAudioFocus()
            mediaPlayer!!.setDataSource(playingSong!!.path)
            mediaPlayer!!.prepare()

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    override fun getMediaPlayer(): MediaPlayer? {
        return mediaPlayer
    }

    override fun onPrepared(mediaPlayer: MediaPlayer) {

        startUpdatingCallbackWithPosition()
        setStatus(PlaybackInfoListener.State.PLAYING)
    }

    override fun openEqualizer(activity: Activity) {
        EqualizerUtils.openEqualizer(activity, mediaPlayer!!)
    }

    override fun release() {
        if (isMediaPlayer()) {
            EqualizerUtils.closeAudioEffectSession(context, mediaPlayer!!.audioSessionId)
            mediaPlayer!!.release()
            mediaPlayer = null
            giveUpAudioFocus()
            unregisterActionsReceiver()
        }
    }

    override fun isPlaying(): Boolean {
        return isMediaPlayer() && mediaPlayer!!.isPlaying
    }

    override fun resumeOrPause() {

        if (isPlaying()) {
            pauseMediaPlayer()
        } else {
            resumeMediaPlayer()
        }
    }

    @PlaybackInfoListener.State
    override fun getState(): Int {
        return state
    }

    override fun isMediaPlayer(): Boolean {
        return mediaPlayer != null
    }

    override fun reset() {
        isReplaySong = !isReplaySong
    }

    override fun isReset(): Boolean {
        return isReplaySong
    }

    override fun skip(isNext: Boolean) {
        getSkipSong(isNext)
    }

    private fun getSkipSong(isNext: Boolean) {
        val currentIndex = songs!!.indexOf(playingSong)

        val index: Int

        try {
            index = if (isNext) currentIndex + 1 else currentIndex - 1
            playingSong = songs!![index]
        } catch (e: IndexOutOfBoundsException) {
            playingSong = if (currentIndex != 0) songs!![0] else songs!![songs!!.size - 1]
            e.printStackTrace()
        }

        initMediaPlayer()
    }

    override fun seekTo(position: Int) {
        if (isMediaPlayer()) {
            mediaPlayer!!.seekTo(position)
        }
    }

    override fun getPlayerPosition(): Int {
        return mediaPlayer!!.currentPosition
    }

    /**
     * Reconfigures the player according to audio focus settings and starts/restarts it. This method
     * starts/restarts the MediaPlayer instance respecting the current audio focus state. So if we
     * have focus, it will play normally; if we don't have focus, it will either leave the player
     * paused or set it to a low volume, depending on what is permitted by the current focus
     * settings.
     */
    private fun configurePlayerState() {

        if (currentAudioFocusState == AUDIO_NO_FOCUS_NO_DUCK) {
            // We don't have audio focus and can't duck, so we have to pause
            pauseMediaPlayer()
        } else {

            if (currentAudioFocusState == AUDIO_NO_FOCUS_CAN_DUCK) {
                // We're permitted to play, but only if we 'duck', ie: play softly
                mediaPlayer!!.setVolume(VOLUME_DUCK, VOLUME_DUCK)
            } else {
                mediaPlayer!!.setVolume(VOLUME_NORMAL, VOLUME_NORMAL)
            }

            // If we were playing when we lost focus, we need to resume playing.
            if (playOnFocusGain) {
                resumeMediaPlayer()
                playOnFocusGain = false
            }
        }
    }

    private inner class NotificationReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (action != null) {

                when (action) {
                    BXNotificationManager.PREV_ACTION -> instantReset()
                    BXNotificationManager.PLAY_PAUSE_ACTION -> resumeOrPause()
                    BXNotificationManager.NEXT_ACTION -> skip(true)

                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> if (playingSong != null) {
                        pauseMediaPlayer()
                    }
                    BluetoothDevice.ACTION_ACL_CONNECTED -> if (playingSong != null && !isPlaying()) {
                        resumeMediaPlayer()
                    }
                    Intent.ACTION_HEADSET_PLUG -> if (playingSong != null) {
                        when (intent.getIntExtra("state", -1)) {
                            //0 means disconnected
                            0 -> pauseMediaPlayer()
                            //1 means connected
                            1 -> if (!isPlaying()) {
                                resumeMediaPlayer()
                            }
                        }
                    }
                }//                    case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                //                        if (isPlaying()) {
                //                            pauseMediaPlayer();
                //                        }
                //                        break;
            }
        }
    }

    private fun callStateListener() {
        // Get the telephony manager
        telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        //Starting listening for PhoneState changes
        phoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, incomingNumber: String) {
                when (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    TelephonyManager.CALL_STATE_OFFHOOK, TelephonyManager.CALL_STATE_RINGING -> if (isPlaying()) {
                        pauseMediaPlayer()
                        ongoingCall = true
                    }
                    TelephonyManager.CALL_STATE_IDLE ->
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false
                                resumeMediaPlayer()
                            }
                        }
                }
            }
        }
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager!!.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE)
    }

    internal fun registerNoisyReceiver() {
        val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        mMusicService!!.registerReceiver(noisyReceiver, intentFilter)
    }

    companion object {

        // The volume we set the media player to when we lose audio focus, but are
        // allowed to reduce the volume instead of stopping playback.
        private const val VOLUME_DUCK = 0.2f
        // The volume we set the media player when we have audio focus.
        private const val VOLUME_NORMAL = 1.0f
        // we don't have audio focus, and can't duck (play at a low volume)
        private const val AUDIO_NO_FOCUS_NO_DUCK = 0
        // we don't have focus, but can duck (play at a low volume)
        private const val AUDIO_NO_FOCUS_CAN_DUCK = 1
        private const val AUDIO_FOCUSED = 2
    }

}
