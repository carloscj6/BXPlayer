package com.revosleap.proxima.ui.fragments

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
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import com.revosleap.proxima.R
import com.revosleap.proxima.callbacks.BXColor
import com.revosleap.proxima.models.Song
import com.revosleap.proxima.services.MusicPlayerService
import com.revosleap.proxima.ui.activities.PlayerActivity
import com.revosleap.proxima.utils.playback.BXNotificationManager
import com.revosleap.proxima.utils.playback.PlaybackInfoListener
import com.revosleap.proxima.callbacks.PlayerAdapter
import com.revosleap.proxima.utils.utils.GetAudio
import com.revosleap.proxima.utils.utils.PreferenceHelper
import com.revosleap.proxima.utils.utils.Universal
import com.revosleap.simpleadapter.SimpleAdapter
import com.revosleap.simpleadapter.SimpleCallbacks
import kotlinx.android.synthetic.main.fragment_tracks.*
import kotlinx.android.synthetic.main.track.view.*


class FragmentTracks : Fragment(), SimpleCallbacks,BXColor {
    private var serviceBound = false
    private var list = mutableListOf<Song>()
    private var mMusicService: MusicPlayerService? = null
    private var mIsBound: Boolean = false
    private var simpleAdapter: SimpleAdapter? = null
    private var mPlayerAdapter: PlayerAdapter? = null
    private var mMusicNotificationManager: BXNotificationManager? = null
    private var mPlaybackListener: PlaybackListener? = null
    private var preferenceHelper: PreferenceHelper? = null
    private var playerActivity:PlayerActivity?=null
    private var viewColor =0
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

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        playerActivity= activity!! as PlayerActivity
        doBindService()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_tracks, container, false)
        list = GetAudio().geAllAudio(activity!!.baseContext)
        simpleAdapter = SimpleAdapter(R.layout.track, this)
        preferenceHelper = PreferenceHelper(activity!!)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getSongList()
        trackRecycler.apply {
            adapter = simpleAdapter
            layoutManager = LinearLayoutManager(activity)
            hasFixedSize()
        }
        buttonListPlayAll.setOnClickListener {
            onSongSelected(list[0], list)
        }
        buttonListShuffle.setOnClickListener {
            list.shuffle()
            onSongSelected(list[0], list)
        }
        buttonListSort.setOnClickListener {
            getSorting(it)
        }
        if (viewColor!=0){
            setColors(viewColor)
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewColor!=0){
            setColors(viewColor)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("serviceStatus", serviceBound)
    }

    override fun onDestroy() {
        super.onDestroy()
        super.onDestroy()
        mPlaybackListener = null
        doUnbindService()
    }
    override fun songColor(color: Int) {
        viewColor = color
       setColors(color)

    }
    private fun setColors(color: Int){
        buttonListSort?.setColorFilter(color)
        buttonListShuffle?.setColorFilter(color)
        buttonListPlayAll?.setColorFilter(color)
    }
    private fun onSongSelected(song: Song, songs:MutableList<Song>) {
        mPlayerAdapter!!.setCurrentSong(song, songs)
        mPlayerAdapter!!.initMediaPlayer()
        mPlayerAdapter!!.getMediaPlayer()?.start()
        mMusicService!!.startForeground(BXNotificationManager.NOTIFICATION_ID,
                mMusicNotificationManager!!.createNotification())


    }

    override fun bindView(view: View, item: Any, position: Int) {
        item as Song
        val title = view.textViewTitleTrack
        val artist = view.textViewArtistTrack
        val image = view.imageView2
        artist.text = item.title
        title.text = item.artist

    }

    override fun onViewClicked(view: View, item: Any, position: Int) {
        onSongSelected(list[position], list)
    }

    override fun onViewLongClicked(it: View?, item: Any, position: Int) {

    }

    private fun getSongList() {
        val sortOrder = preferenceHelper?.sortingStyle
        if (sortOrder!!.isEmpty()) {
            simpleAdapter?.addManyItems(list.toMutableList())
        }
        if (sortOrder == Universal.SORT_BY_ARTIST) {
            val sorted = list.sortedWith(compareBy { it.artist })
            simpleAdapter?.changeItems(sorted.toMutableList())
            list = sorted.toMutableList()
        }
        if (sortOrder == Universal.SORT_BY_TITLE) {
            val sorted = list.sortedWith(compareBy { it.title })
            simpleAdapter?.changeItems(sorted.toMutableList())
            list = sorted.toMutableList()
        }
        if (sortOrder == Universal.SORT_BY_YEAR) {
            val dateSorted = list.sortedWith(compareBy { it.songYear })
            simpleAdapter?.changeItems(dateSorted.toMutableList())
            list = dateSorted.toMutableList()
        }
    }

    private fun getSorting(view: View) {
        val menu = PopupMenu(activity!!, view)
        menu.menuInflater.inflate(R.menu.track_sorting, menu.menu)
        menu.show()
        menu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(p0: MenuItem?): Boolean {
                when (p0?.itemId) {
                    R.id.item_artist -> {
                       preferenceHelper?.sortingStyle = Universal.SORT_BY_ARTIST
                        val sorted = list.sortedWith(compareBy { it.artist })
                        simpleAdapter?.changeItems(sorted.toMutableList())
                        list = sorted.toMutableList()
                    }
                    R.id.item_date -> {
                        preferenceHelper?.sortingStyle = Universal.SORT_BY_YEAR
                        val dateSorted = list.sortedWith(compareBy { it.songYear })
                        simpleAdapter?.changeItems(dateSorted.toMutableList())
                        list = dateSorted.toMutableList()
                    }
                    R.id.item_title -> {
                       preferenceHelper?.sortingStyle = Universal.SORT_BY_TITLE
                        val sorted = list.sortedWith(compareBy { it.title })
                        simpleAdapter?.changeItems(sorted.toMutableList())
                        list = sorted.toMutableList()
                    }


                }
                return true
            }
        })
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

        }

        override fun onStateChanged(@State state: Int) {
            if (mPlayerAdapter?.getState() != State.RESUMED && mPlayerAdapter?.getState() != State.PAUSED) {
                updatePlayingInfo(false, startPlay = true)
            }
        }

        override fun onPlaybackCompleted() {

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
        val song =mPlayerAdapter?.getCurrentSong()!!
        playerActivity?.updatePlaying(song)
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
    // val retriever = MediaMetadataRetriever()
//        val inputStream: InputStream?
//        retriever.setDataSource(path)
//        if (retriever.embeddedPicture != null) {
//            inputStream = ByteArrayInputStream(retriever.embeddedPicture)
//            Glide.with(holder.itemView.context).load(inputStream)
//                    .into(holder.trackImage)
//        }

}
