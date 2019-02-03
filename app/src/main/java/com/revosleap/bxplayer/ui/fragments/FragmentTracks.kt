package com.revosleap.bxplayer.ui.fragments

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.revosleap.bxplayer.R
import com.revosleap.bxplayer.services.MusicPlayerService
import com.revosleap.bxplayer.utils.adapters.TrackAdapter
import com.revosleap.bxplayer.models.AudioModel
import com.revosleap.bxplayer.utils.playback.BXNotificationManager
import com.revosleap.bxplayer.utils.playback.PlaybackInfoListener
import com.revosleap.bxplayer.utils.playback.PlayerAdapter
import com.revosleap.bxplayer.utils.utils.GetAudio
import kotlinx.android.synthetic.main.fragment_tracks.*


class FragmentTracks : Fragment(), TrackAdapter.SongSelectedListener {


    private var serviceBound = false
    private var list = mutableListOf<AudioModel>()
    private var mMusicService: MusicPlayerService? = null
    private var mIsBound: Boolean = false
    private var trackAdapter: TrackAdapter? = null
    private var mPlayerAdapter: PlayerAdapter? = null
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


        val view = inflater.inflate(R.layout.fragment_tracks, container, false)


        list = GetAudio().geAllAudio(activity!!.baseContext)
        trackAdapter = TrackAdapter(list, activity!!, this)

        return view


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        trackRecycler.apply {
            adapter = trackAdapter
            layoutManager = LinearLayoutManager(activity)
            hasFixedSize()
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        doBindService()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("serviceStatus", serviceBound)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        //        serviceBound=savedInstanceState.getBoolean("serviceStatus");
    }

    override fun onDestroy() {
        super.onDestroy()
        super.onDestroy()
        mPlaybackListener = null
        doUnbindService()
    }

    override fun onSongSelected(song: AudioModel, songs: List<AudioModel>) {
        mPlayerAdapter!!.setCurrentSong(song, songs)
        mPlayerAdapter!!.initMediaPlayer()
        mPlayerAdapter!!.getMediaPlayer()?.start()
        mMusicService!!.startForeground(BXNotificationManager.NOTIFICATION_ID,
                mMusicNotificationManager!!.createNotification())


    }

    private fun doBindService() {
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

    internal inner class PlaybackListener : PlaybackInfoListener() {

        override fun onPositionChanged(position: Int) {
            //            if (!mUserIsSeeking) {
            //                mSeekBarAudio.setProgress(position);
            //            }
        }

        override fun onStateChanged(@State state: Int) {
            //
            //            updatePlayingStatus();
            if (mPlayerAdapter?.getState() != State.RESUMED && mPlayerAdapter?.getState() != State.PAUSED) {
                updatePlayingInfo(false, true);
            }
        }

        override fun onPlaybackCompleted() {
            //updateResetStatus(true);
        }
    }

    private fun updatePlayingInfo(restore: Boolean, startPlay: Boolean) {
        if (startPlay) {
            mPlayerAdapter!!.getMediaPlayer()?.start()
            Handler().postDelayed({
                mMusicService!!.startForeground(BXNotificationManager.NOTIFICATION_ID,
                        mMusicNotificationManager!!.createNotification())
            }, 250)
        }
        if (restore) {
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

    companion object {
        const val Broadcast_PLAY_NEW_AUDIO = "com.revosleap.bxplayer.PlayNewAudio"
    }
}
