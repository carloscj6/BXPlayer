package com.revosleap.proxima.ui.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.revosleap.proxima.R
import com.revosleap.proxima.models.Song
import com.revosleap.proxima.services.MusicPlayerService
import com.revosleap.proxima.ui.activities.PlayerActivity
import com.revosleap.proxima.utils.playback.BXNotificationManager
import com.revosleap.proxima.utils.playback.PlaybackInfoListener
import com.revosleap.proxima.callbacks.PlayerAdapter
import com.revosleap.proxima.utils.utils.Universal
import com.revosleap.simpleadapter.SimpleAdapter
import com.revosleap.simpleadapter.SimpleCallbacks
import kotlinx.android.synthetic.main.fragment_album_details.*
import kotlinx.android.synthetic.main.track.view.*
import java.lang.reflect.Type

class FragmentAlbumInfo : Fragment(), SimpleCallbacks {
    private var albumString: String? = null
    private var playerActivity: PlayerActivity? = null
    private var songs = mutableListOf<Song>()
    private var simpleAdapter: SimpleAdapter? = null
    private var mPlayerAdapter: PlayerAdapter? = null
    private var mMusicService: MusicPlayerService? = null
    private var mMusicNotificationManager: BXNotificationManager? = null
    private var mPlaybackListener: PlaybackListener? = null
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        playerActivity = activity!! as PlayerActivity
        getService()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        albumString = arguments?.getString(Universal.ALBUM_BUNDLE)
        simpleAdapter = SimpleAdapter(R.layout.track, this)
        getMusicList()
        return inflater.inflate(R.layout.fragment_album_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewAlbum.apply {
            adapter = simpleAdapter
            layoutManager = LinearLayoutManager(playerActivity)
            hasFixedSize()
        }
        simpleAdapter?.addManyItems(songs.toMutableList())
        setButtons()
        val params = cordinator.layoutParams as ViewGroup.MarginLayoutParams
        params.bottomMargin = playerActivity?.controls()!!.height
        cordinator.layoutParams = params
        textViewAlbumArtist.text = songs[0].artist
        textViewAlbumName.text=songs[0].albumName
    }

    override fun onResume() {
        super.onResume()
        getService()
    }

    override fun bindView(view: View, item: Any, position: Int) {
        item as Song
        val titleText = view.textViewTitleTrack
        val artistText = view.textViewArtistTrack
        titleText.text = item.artist
        artistText.text = item.title
    }

    override fun onViewClicked(view: View, item: Any, position: Int) {
        onSongSelected(songs[position], songs)
    }

    override fun onViewLongClicked(it: View?, item: Any, position: Int) {

    }
    private fun setButtons(){
     buttonListPlayAll.setOnClickListener {
         onSongSelected(songs[0],songs)
     }
        buttonListShuffle.setOnClickListener {
            songs.shuffle()
            onSongSelected(songs[0],songs)
        }
    }
    private fun onSongSelected(song: Song, songs: MutableList<Song>) {
        mPlayerAdapter!!.setCurrentSong(song, songs)
        mPlayerAdapter!!.initMediaPlayer()
        mPlayerAdapter!!.getMediaPlayer()?.start()
        mMusicService!!.startForeground(BXNotificationManager.NOTIFICATION_ID,
                mMusicNotificationManager!!.createNotification())


    }

    private fun getMusicList() {
        if (albumString != null) {
            val gson = Gson()
            val type: Type = object : TypeToken<MutableList<Song>>() {}.type
            val songs = gson.fromJson<MutableList<Song>>(albumString, type)
            if (songs != null && songs.size > 0) {
                this.songs = songs
            }
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