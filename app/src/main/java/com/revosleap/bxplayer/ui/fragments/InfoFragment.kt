package com.revosleap.bxplayer.ui.fragments


import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import com.revosleap.bxplayer.R
import com.revosleap.bxplayer.services.MusicPlayerService
import com.revosleap.bxplayer.utils.playback.BXNotificationManager
import com.revosleap.bxplayer.utils.playback.PlaybackInfoListener
import com.revosleap.bxplayer.utils.playback.PlayerAdapter
import com.revosleap.bxplayer.utils.utils.AudioUtils
import com.revosleap.bxplayer.utils.utils.EqualizerUtils
import kotlinx.android.synthetic.main.info.*


class InfoFragment : Fragment(), View.OnClickListener {
    private var mUserIsSeeking: Boolean = false
    private var mIsBound: Boolean = false
    private var mSelectedArtist: String? = null
    private var mPlayerAdapter: PlayerAdapter? = null
    internal var mMusicService: MusicPlayerService? = null
    private var mMusicNotificationManager: BXNotificationManager? = null
    private var mPlaybackListener: PlaybackListener? = null
    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mMusicService = (service as MusicPlayerService.MusicBinder).serviceInstance
            mPlayerAdapter = mMusicService!!.mediaPlayerHolder
            mMusicNotificationManager = mMusicService!!.musicNotificationManager
            if (mPlaybackListener == null) {
                mPlaybackListener = PlaybackListener()
                mPlayerAdapter!!.setPlaybackInfoListener(mPlaybackListener!!)
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mMusicService = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.info, container, false)
        doBindService()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeSeekbar()
        setClickListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        doUnbindService()
    }

    override fun onPause() {
        super.onPause()
        doUnbindService()
    }

    override fun onResume() {
        super.onResume()
        doBindService()
    }

    private fun doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        activity!!.bindService(Intent(activity,
                MusicPlayerService::class.java), mConnection, Context.BIND_AUTO_CREATE)
        mIsBound = true

        val startNotStickyIntent = Intent(activity, MusicPlayerService::class.java)
        activity!!.startService(startNotStickyIntent)
    }

    private fun doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            activity!!.unbindService(mConnection)
            mIsBound = false
        }
    }


    private fun initializeSeekbar() {
        seekBarInfo.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            internal var userSelectedPosition = 0

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    userSelectedPosition = progress

                }
                textViewProgress?.text = AudioUtils.formatDuration(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                mUserIsSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (mUserIsSeeking) {
                    //  mSongPosition.setTextColor(currentPositionColor);
                }
                mUserIsSeeking = false
                mPlayerAdapter!!.seekTo(userSelectedPosition)
            }
        })
    }

    private fun updatePlayingStatus() {
        val drawable = if (mPlayerAdapter!!.state != PlaybackInfoListener.State.PAUSED)
            R.drawable.pause
        else
            R.drawable.play_icon
        try {
            buttonInfoPlay.post(Runnable { buttonInfoPlay.setBackgroundResource(drawable) })
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun updatePlayingInfo(restore: Boolean, startPlay: Boolean) {


        if (startPlay) {
            mPlayerAdapter!!.mediaPlayer.start()
            Handler().postDelayed({
                mMusicService!!.startForeground(BXNotificationManager.NOTIFICATION_ID,
                        mMusicNotificationManager!!.createNotification())
            }, 250)
        }

        val selectedSong = mPlayerAdapter!!.currentSong

        mSelectedArtist = selectedSong.artist
        val duration = selectedSong.duration
        seekBarInfo?.max = duration
        textViewDuration?.text = AudioUtils.formatDuration(duration)

        textViewInfoTitle?.post(Runnable { textViewInfoTitle.setText(selectedSong.title) })

        textViewInfoArtist?.setText(selectedSong.artist)

        if (restore) {
            seekBarInfo?.setProgress(mPlayerAdapter!!.playerPosition)
            updatePlayingStatus()
            // updateResetStatus(false);

            Handler().postDelayed({
                //stop foreground if coming from pause state
                if (mMusicService!!.isRestoredFromPause) {
                    mMusicService!!.stopForeground(false)
                    mMusicService!!.musicNotificationManager?.notificationManager!!
                            .notify(BXNotificationManager.NOTIFICATION_ID,
                                    mMusicService!!.musicNotificationManager?.notificationBuilder?.build())
                    mMusicService!!.isRestoredFromPause = false
                }
            }, 250)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.buttonInfoPlaylist -> {
            }
            R.id.buttonInfoFave -> {
            }
            R.id.buttonInfoVol -> {
            }
            R.id.buttonInfoShuffle -> {
            }
            R.id.buttonInfoPrev -> skipPrev()
            R.id.buttonInfoPlay -> resumeOrPause()
            R.id.buttonInfoNext -> skipNext()
            R.id.buttonInfoAll -> {
            }
        }
    }

    private fun setClickListeners() {
        buttonInfoAll.setOnClickListener(this)
        buttonInfoFave.setOnClickListener(this)
        buttonInfoNext.setOnClickListener(this)
        buttonInfoPlay.setOnClickListener(this)
        buttonInfoPlaylist.setOnClickListener(this)
        buttonInfoPrev.setOnClickListener(this)
        buttonInfoShuffle.setOnClickListener(this)
        buttonInfoVol.setOnClickListener(this)
    }

    internal inner class PlaybackListener : PlaybackInfoListener() {

        override fun onPositionChanged(position: Int) {
            if (!mUserIsSeeking) {
                seekBarInfo.setProgress(position)
            }
        }

        override fun onStateChanged(@State state: Int) {

            updatePlayingStatus()
            if (mPlayerAdapter!!.state != PlaybackInfoListener.State.RESUMED && mPlayerAdapter!!.state != PlaybackInfoListener.State.PAUSED) {
                updatePlayingInfo(false, true)
            }
        }

        override fun onPlaybackCompleted() {
            //updateResetStatus(true);
        }
    }

    private fun checkIsPlayer(): Boolean {

        val isPlayer = mPlayerAdapter!!.isMediaPlayer
        if (!isPlayer) {
            EqualizerUtils.notifyNoSessionId(activity!!)
        }
        return isPlayer
    }

    fun skipPrev() {
        if (checkIsPlayer()) {
            mPlayerAdapter!!.instantReset()
        }
    }

    fun resumeOrPause() {
        if (checkIsPlayer()) {
            mPlayerAdapter!!.resumeOrPause()
        }
    }

    fun skipNext() {
        if (checkIsPlayer()) {
            mPlayerAdapter!!.skip(true)
        }
    }

    fun openEqualizer() {
        if (EqualizerUtils.hasEqualizer(activity!!)) {
            if (checkIsPlayer()) {
                mPlayerAdapter!!.openEqualizer(activity!!)
            }
        } else {
            Toast.makeText(activity, "No equilizer found", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {

        fun update() {

        }
    }
}
