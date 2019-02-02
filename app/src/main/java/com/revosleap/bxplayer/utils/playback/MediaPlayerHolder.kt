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
import com.revosleap.bxplayer.services.MusicPlayerService
import com.revosleap.bxplayer.utils.models.Album
import com.revosleap.bxplayer.utils.models.AudioModel
import com.revosleap.bxplayer.utils.player.EqualizerUtils
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


class MediaPlayerHolder internal constructor(private val mMusicService: MusicPlayerService?)
    : PlayerAdapter, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {
    // we have full audio focus
    private var ongoingCall: Boolean = false
    private val mContext: Context = mMusicService!!.applicationContext
    private val mAudioManager: AudioManager
    private var mMediaPlayer: MediaPlayer? = null
    private var mPlaybackInfoListener: PlaybackInfoListener? = null
    private var mExecutor: ScheduledExecutorService? = null
    private var mSeekBarPositionUpdateTask: Runnable? = null
    private var mSelectedSong: AudioModel? = null
    private var mSongs: List<AudioModel>? = null
    private var mSelectedAlbum: Album? = null
    private var sReplaySong = false
    @PlaybackInfoListener.State
    private var mState: Int = 0
    private val mediaSession: MediaSessionCompat? = null
    private var mNotificationActionsReceiver: NotificationReceiver? = null
    private var mMusicNotificationManager: BXNotificationManager? = null
    private var mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK
    private var mPlayOnFocusGain: Boolean = false
    private var phoneStateListener: PhoneStateListener? = null
    private var telephonyManager: TelephonyManager? = null
    private val mOnAudioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> mCurrentAudioFocusState = AUDIO_FOCUSED
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->
                // Audio focus was lost, but it's possible to duck (i.e.: play quietly)
                mCurrentAudioFocusState = AUDIO_NO_FOCUS_CAN_DUCK
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Lost audio focus, but will gain it back (shortly), so note whether
                // playback should resume
                mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK
                mPlayOnFocusGain = isMediaPlayer && mState == PlaybackInfoListener.State.PLAYING || mState == PlaybackInfoListener.State.RESUMED
            }
            AudioManager.AUDIOFOCUS_LOSS ->
                // Lost audio focus, probably "permanently"
                mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK
        }

        if (mMediaPlayer != null) {
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
        mAudioManager = mContext.getSystemService(AUDIO_SERVICE) as AudioManager
    }

    private fun registerActionsReceiver() {
        mNotificationActionsReceiver = NotificationReceiver()
        val intentFilter = IntentFilter()

        intentFilter.addAction(BXNotificationManager.PREV_ACTION)
        intentFilter.addAction(BXNotificationManager.PLAY_PAUSE_ACTION)
        intentFilter.addAction(BXNotificationManager.NEXT_ACTION)
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG)
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)

        mMusicService!!.registerReceiver(mNotificationActionsReceiver, intentFilter)
    }

    private fun unregisterActionsReceiver() {
        if (mMusicService != null && mNotificationActionsReceiver != null) {
            try {
                mMusicService.unregisterReceiver(mNotificationActionsReceiver)
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

    override fun getCurrentSong(): AudioModel? {
        return mSelectedSong
    }

    override fun getSelectedAlbum(): Album? {
        return mSelectedAlbum
    }

    override fun setSelectedAlbum(album: Album) {
        mSelectedAlbum = album
    }

    override fun setCurrentSong(song: AudioModel, songs: List<AudioModel>) {
        mSelectedSong = song
        mSongs = songs
    }

    override fun onCompletion(mediaPlayer: MediaPlayer) {
        if (mPlaybackInfoListener != null) {
            mPlaybackInfoListener!!.onStateChanged(PlaybackInfoListener.State.COMPLETED)
            mPlaybackInfoListener!!.onPlaybackCompleted()
        }

        if (sReplaySong) {
            if (isMediaPlayer()) {
                resetSong()
            }
            sReplaySong = false
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

        val result = mAudioManager.requestAudioFocus(
                mOnAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mCurrentAudioFocusState = AUDIO_FOCUSED
        } else {
            mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK
        }
    }

    private fun giveUpAudioFocus() {
        if (mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK
        }
    }

    override fun setPlaybackInfoListener(listener: PlaybackInfoListener) {
        mPlaybackInfoListener = listener
    }

    private fun setStatus(@PlaybackInfoListener.State state: Int) {

        mState = state
        if (mPlaybackInfoListener != null) {
            mPlaybackInfoListener!!.onStateChanged(state)
        }
    }

    private fun resumeMediaPlayer() {
        if (!isPlaying()) {
            mMediaPlayer!!.start()
            setStatus(PlaybackInfoListener.State.RESUMED)
            mMusicService!!.startForeground(BXNotificationManager.NOTIFICATION_ID,
                    mMusicNotificationManager!!.createNotification())
        }
    }

    fun pauseMediaPlayer() {
        setStatus(PlaybackInfoListener.State.PAUSED)
        mMediaPlayer!!.pause()
        mMusicService!!.stopForeground(false)
        mMusicNotificationManager!!.notificationManager.notify(BXNotificationManager.NOTIFICATION_ID,
                mMusicNotificationManager!!.createNotification())
    }

    private fun resetSong() {
        mMediaPlayer!!.seekTo(0)
        mMediaPlayer!!.start()
        setStatus(PlaybackInfoListener.State.PLAYING)
    }

    /**
     * Syncs the mMediaPlayer position with mPlaybackProgressCallback via recurring task.
     */
    private fun startUpdatingCallbackWithPosition() {
        if (mExecutor == null) {
            mExecutor = Executors.newSingleThreadScheduledExecutor()
        }
        if (mSeekBarPositionUpdateTask == null) {
            mSeekBarPositionUpdateTask = Runnable { updateProgressCallbackTask() }
        }

        mExecutor!!.scheduleAtFixedRate(
                mSeekBarPositionUpdateTask,
                0,
                1000,
                TimeUnit.MILLISECONDS
        )
    }

    // Reports media playback position to mPlaybackProgressCallback.
    private fun stopUpdatingCallbackWithPosition() {
        if (mExecutor != null) {
            mExecutor!!.shutdownNow()
            mExecutor = null
            mSeekBarPositionUpdateTask = null
        }
    }

    private fun updateProgressCallbackTask() {
        if (isMediaPlayer() && mMediaPlayer!!.isPlaying) {
            val currentPosition = mMediaPlayer!!.currentPosition
            if (mPlaybackInfoListener != null) {
                mPlaybackInfoListener!!.onPositionChanged(currentPosition)
            }
        }
    }

    override fun instantReset() {
        if (isMediaPlayer()) {
            if (mMediaPlayer!!.currentPosition < 5000) {
                skip(false)
            } else {
                resetSong()
            }
        }
    }

    /**
     * Once the [MediaPlayer] is released, it can't be used again, and another one has to be
     * created. In the onStop() method of the [com.revosleap.bxplayer.PlayerActivity] the [MediaPlayer] is
     * released. Then in the onStart() of the [com.revosleap.bxplayer.PlayerActivity] a new [MediaPlayer]
     * object has to be created. That's why this method is private, and called by load(int) and
     * not the constructor.
     */
    override fun initMediaPlayer() {

        try {
            if (mMediaPlayer != null) {
                mMediaPlayer!!.reset()
            } else {
                mMediaPlayer = MediaPlayer()
                EqualizerUtils.openAudioEffectSession(mContext, mMediaPlayer!!.audioSessionId)

                mMediaPlayer!!.setOnPreparedListener(this)
                mMediaPlayer!!.setOnCompletionListener(this)
                mMediaPlayer!!.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK)
                mMediaPlayer!!.setAudioAttributes(AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                mMusicNotificationManager = mMusicService!!.musicNotificationManager
                callStateListener()

            }
            tryToGetAudioFocus()
            mMediaPlayer!!.setDataSource(mSelectedSong!!.path)
            mMediaPlayer!!.prepare()

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    override fun getMediaPlayer(): MediaPlayer? {
        return mMediaPlayer
    }

    override fun onPrepared(mediaPlayer: MediaPlayer) {

        startUpdatingCallbackWithPosition()
        setStatus(PlaybackInfoListener.State.PLAYING)
    }

    override fun openEqualizer(activity: Activity) {
        EqualizerUtils.openEqualizer(activity, mMediaPlayer!!)
    }

    override fun release() {
        if (isMediaPlayer()) {
            EqualizerUtils.closeAudioEffectSession(mContext, mMediaPlayer!!.audioSessionId)
            mMediaPlayer!!.release()
            mMediaPlayer = null
            giveUpAudioFocus()
            unregisterActionsReceiver()
        }
    }

    override fun isPlaying(): Boolean {
        return isMediaPlayer() && mMediaPlayer!!.isPlaying
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
        return mState
    }

    override fun isMediaPlayer(): Boolean {
        return mMediaPlayer != null
    }

    override fun reset() {
        sReplaySong = !sReplaySong
    }

    override fun isReset(): Boolean {
        return sReplaySong
    }

    override fun skip(isNext: Boolean) {
        getSkipSong(isNext)
    }

    private fun getSkipSong(isNext: Boolean) {
        val currentIndex = mSongs!!.indexOf(mSelectedSong)

        val index: Int

        try {
            index = if (isNext) currentIndex + 1 else currentIndex - 1
            mSelectedSong = mSongs!![index]
        } catch (e: IndexOutOfBoundsException) {
            mSelectedSong = if (currentIndex != 0) mSongs!![0] else mSongs!![mSongs!!.size - 1]
            e.printStackTrace()
        }

        initMediaPlayer()
    }

    override fun seekTo(position: Int) {
        if (isMediaPlayer()) {
            mMediaPlayer!!.seekTo(position)
        }
    }

    override fun getPlayerPosition(): Int {
        return mMediaPlayer!!.currentPosition
    }

    /**
     * Reconfigures the player according to audio focus settings and starts/restarts it. This method
     * starts/restarts the MediaPlayer instance respecting the current audio focus state. So if we
     * have focus, it will play normally; if we don't have focus, it will either leave the player
     * paused or set it to a low volume, depending on what is permitted by the current focus
     * settings.
     */
    private fun configurePlayerState() {

        if (mCurrentAudioFocusState == AUDIO_NO_FOCUS_NO_DUCK) {
            // We don't have audio focus and can't duck, so we have to pause
            pauseMediaPlayer()
        } else {

            if (mCurrentAudioFocusState == AUDIO_NO_FOCUS_CAN_DUCK) {
                // We're permitted to play, but only if we 'duck', ie: play softly
                mMediaPlayer!!.setVolume(VOLUME_DUCK, VOLUME_DUCK)
            } else {
                mMediaPlayer!!.setVolume(VOLUME_NORMAL, VOLUME_NORMAL)
            }

            // If we were playing when we lost focus, we need to resume playing.
            if (mPlayOnFocusGain) {
                resumeMediaPlayer()
                mPlayOnFocusGain = false
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

                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> if (mSelectedSong != null) {
                        pauseMediaPlayer()
                    }
                    BluetoothDevice.ACTION_ACL_CONNECTED -> if (mSelectedSong != null && !isPlaying()) {
                        resumeMediaPlayer()
                    }
                    Intent.ACTION_HEADSET_PLUG -> if (mSelectedSong != null) {
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
        telephonyManager = mContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
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
                        if (mMediaPlayer != null) {
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
        private val VOLUME_DUCK = 0.2f
        // The volume we set the media player when we have audio focus.
        private val VOLUME_NORMAL = 1.0f
        // we don't have audio focus, and can't duck (play at a low volume)
        private val AUDIO_NO_FOCUS_NO_DUCK = 0
        // we don't have focus, but can duck (play at a low volume)
        private val AUDIO_NO_FOCUS_CAN_DUCK = 1
        private val AUDIO_FOCUSED = 2
    }

}
