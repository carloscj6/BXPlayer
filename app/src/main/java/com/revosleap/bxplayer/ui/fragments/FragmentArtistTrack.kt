package com.revosleap.bxplayer.ui.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.revosleap.bxplayer.R
import com.revosleap.bxplayer.callbacks.PlayerAdapter
import com.revosleap.bxplayer.models.Song
import com.revosleap.bxplayer.services.MusicPlayerService
import com.revosleap.bxplayer.ui.activities.PlayerActivity
import com.revosleap.bxplayer.utils.playback.BXNotificationManager
import com.revosleap.bxplayer.utils.playback.PlaybackInfoListener
import com.revosleap.bxplayer.utils.utils.Universal
import com.revosleap.bxplayer.utils.utils.UniversalUtils
import com.revosleap.simpleadapter.SimpleAdapter
import com.revosleap.simpleadapter.SimpleCallbacks
import kotlinx.android.synthetic.main.artist_track_item.view.*
import kotlinx.android.synthetic.main.fragment_tracks.*
import java.lang.reflect.Type

class FragmentArtistTrack : Fragment(), SimpleCallbacks {
    private var playerActivity: PlayerActivity? = null
    private var simpleAdapter: SimpleAdapter? = null
    private var songs = mutableListOf<Song>()
    private var mPlayerAdapter: PlayerAdapter? = null
    private var mMusicService: MusicPlayerService? = null
    private var mMusicNotificationManager: BXNotificationManager? = null
    private var mPlaybackListener: PlaybackListener? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        playerActivity = activity as PlayerActivity
        getService()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            : View? {
        simpleAdapter = SimpleAdapter(R.layout.artist_track_item, this)
        getMusicList()
        return inflater.inflate(R.layout.fragment_tracks, container, false)
    }

    override fun bindView(view: View, item: Any, position: Int) {
        item as Song
        val trackNo = view.textViewTrackNumber
        val trackTitle = view.textViewSongTitle
        val trackDuration = view.textViewSongDuration
        trackNo.text = item.trackNumber.toString()
        trackTitle.text = item.title
        trackDuration.text = UniversalUtils.formatTime(item.duration)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        trackRecycler.apply {
            adapter = simpleAdapter
            layoutManager = LinearLayoutManager(playerActivity)
            hasFixedSize()
        }
        simpleAdapter?.addManyItems(songs.toMutableList())
    }


    override fun onResume() {
        super.onResume()
        getService()
    }

    override fun onViewClicked(view: View, item: Any, position: Int) {
        onSongSelected(songs[position], songs)
    }

    override fun onViewLongClicked(it: View?, item: Any, position: Int) {

    }

    private fun onSongSelected(song: Song, songs: MutableList<Song>) {
        mPlayerAdapter!!.setCurrentSong(song, songs)
        mPlayerAdapter!!.initMediaPlayer()
        mPlayerAdapter!!.getMediaPlayer()?.start()
        mMusicService!!.startForeground(BXNotificationManager.NOTIFICATION_ID,
                mMusicNotificationManager!!.createNotification())


    }


    fun getMusicList() {
        val songString = arguments?.getString(Universal.SONGS_BUNDLE)
        val gson = Gson()
        val type: Type = object : TypeToken<MutableList<Song>>() {}.type
        val songs = gson.fromJson<MutableList<Song>>(songString, type)
        if (songs != null && songs.size > 0) {
            this.songs = songs
        }
    }

    private fun getService() {
        mMusicService = playerActivity?.getPlayerService()
        if (mMusicService != null) {
            mPlayerAdapter = mMusicService!!.mediaPlayerHolder
            mMusicNotificationManager = mMusicService!!.musicNotificationManager
        }
        if (mPlaybackListener == null) {
            mPlaybackListener = PlaybackListener()
            mPlayerAdapter?.setPlaybackInfoListener(mPlaybackListener!!)
        }
    }

    private fun updatePlayingInfo(restore: Boolean, startPlay: Boolean) {
        if (startPlay) {
            mPlayerAdapter!!.getMediaPlayer()?.start()
            mMusicService!!.startForeground(BXNotificationManager.NOTIFICATION_ID,
                    mMusicNotificationManager!!.createNotification())

        }
        val currentSong = mPlayerAdapter?.getCurrentSong()
        playerActivity?.updatePlaying(currentSong!!)

        if (restore) {

            if (mMusicService!!.isRestoredFromPause) {
                mMusicService!!.stopForeground(false)
                mMusicService!!.musicNotificationManager?.notificationManager!!
                        .notify(BXNotificationManager.NOTIFICATION_ID,
                                mMusicService!!.musicNotificationManager?.notificationBuilder?.build())
                mMusicService!!.isRestoredFromPause = false
            }

        }
    }

    internal inner class PlaybackListener : PlaybackInfoListener() {
        override fun onPositionChanged(position: Int) {

        }

        override fun onStateChanged(@State state: Int) {
            if (mPlayerAdapter?.getState() != State.RESUMED && mPlayerAdapter?.getState() != State.PAUSED) {
                updatePlayingInfo(false, startPlay = true)
            }
        }

        override fun onPlaybackCompleted() {

        }
    }
}