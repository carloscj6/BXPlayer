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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.revosleap.bxplayer.utils.playback.BXNotificationManager
import com.revosleap.bxplayer.utils.playback.BxPlayerService
import com.revosleap.bxplayer.utils.playback.PlaybackInfoListener
import com.revosleap.bxplayer.utils.playback.PlayerAdapter
import com.revosleap.bxplayer.utils.models.AudioModel
import com.revosleap.bxplayer.utils.player.AudioPlayerService
import com.revosleap.bxplayer.utils.adapters.TrackAdapter
import com.revosleap.bxplayer.utils.utils.GetAudio
import com.revosleap.bxplayer.utils.utils.StorageUtil
import com.revosleap.bxplayer.R
import kotlinx.android.synthetic.main.fragment_tracks.*
import java.util.*


class FragmentTracks : Fragment(), TrackAdapter.SongSelectedListener {

    private var player: AudioPlayerService? = null
    internal var serviceBound = false
    internal var list = mutableListOf<AudioModel>()
    internal var mMusicService: BxPlayerService? = null
    private var mIsBound: Boolean = false
    private var trackAdapter: TrackAdapter? = null
    private var mPlayerAdapter: PlayerAdapter? = null
    private var mMusicNotificationManager: BXNotificationManager? = null
    private var mPlaybackListener: PlaybackListener? = null
    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mMusicService = (service as BxPlayerService.BXBinder).instance
            mPlayerAdapter = mMusicService!!.mediaPlayerHolder
            mMusicNotificationManager = mMusicService!!.musicNotificationManager
            if (mPlaybackListener == null) {
                mPlaybackListener = PlaybackListener()
                mPlayerAdapter!!.setPlaybackInfoListener(mPlaybackListener)
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mMusicService = null
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as AudioPlayerService.LocalBinder
            player = binder.service
            serviceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serviceBound = false
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        val view = inflater.inflate(R.layout.fragment_tracks, container, false)


        list = GetAudio().geAllAudio(activity)
        trackAdapter = TrackAdapter(list, activity, this)

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

    private fun playAudio(audioIndex: Int) {
        val arrayList: ArrayList<AudioModel>? = null
        //Check is service is active
        if (!serviceBound) {
            //Store Serializable audioList to SharedPreferences
            val storage = StorageUtil(activity)
            storage.storeAudio(list)
            storage.storeAudioIndex(audioIndex)

            val playerIntent = Intent(activity, AudioPlayerService::class.java)
            activity!!.startService(playerIntent)
            activity!!.bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        } else {
            //Store the new audioIndex to SharedPreferences
            val storage = StorageUtil(activity)
            storage.storeAudioIndex(audioIndex)

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            val broadcastIntent = Intent(Broadcast_PLAY_NEW_AUDIO)
            activity!!.sendBroadcast(broadcastIntent)
        }
    }


    override fun onSongSelected(song: AudioModel, songs: List<AudioModel>) {
        Log.v("song ", song.title + " number= " + songs.size)
        try {
            mPlayerAdapter!!.setCurrentSong(song, songs)
            mPlayerAdapter!!.initMediaPlayer()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mPlayerAdapter!!.mediaPlayer.start()
        Handler().postDelayed({
            mMusicService!!.startForeground(BXNotificationManager.NOTIFICATION_ID,
                    mMusicNotificationManager!!.createNotification())
        }, 250)

    }

    private fun doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        activity!!.bindService(Intent(activity,
                BxPlayerService::class.java), mConnection, Context.BIND_AUTO_CREATE)
        mIsBound = true

        val startNotStickyIntent = Intent(activity, BxPlayerService::class.java)
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
            //            if (mPlayerAdapter.getState() != State.RESUMED && mPlayerAdapter.getState() != State.PAUSED) {
            //                updatePlayingInfo(false, true);
            //            }
        }

        override fun onPlaybackCompleted() {
            //updateResetStatus(true);
        }
    }

    companion object {
        val Broadcast_PLAY_NEW_AUDIO = "com.revosleap.bxplayer.PlayNewAudio"
    }
}
