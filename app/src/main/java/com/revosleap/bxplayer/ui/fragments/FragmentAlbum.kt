package com.revosleap.bxplayer.ui.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.revosleap.bxplayer.R
import com.revosleap.bxplayer.models.Album
import com.revosleap.bxplayer.models.Song
import com.revosleap.bxplayer.utils.utils.AlbumProvider
import com.revosleap.bxplayer.utils.utils.SongProvider
import com.revosleap.simpleadapter.SimpleAdapter
import com.revosleap.simpleadapter.SimpleCallbacks
import kotlinx.android.synthetic.main.album_item.view.*
import kotlinx.android.synthetic.main.fragment_fragment_album.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.warn


class FragmentAlbum : Fragment(), SimpleCallbacks,AnkoLogger {
    private var musicList = mutableListOf<Song>()
    private var albumList = mutableListOf<Album>()
    var simpleAdapter: SimpleAdapter? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        simpleAdapter = SimpleAdapter(R.layout.album_item, this)
        musicList = SongProvider.getAllDeviceSongs(activity!!)
        info(musicList.size)
        return inflater.inflate(R.layout.fragment_fragment_album, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        albumList = AlbumProvider.retrieveAlbums(musicList)
        warn(albumList.size)
        simpleAdapter?.clearItems()
        simpleAdapter?.addManyItems(albumList.toMutableList())
        simpleAdapter?.setHasStableIds(true)
        recyclerViewAlbum.apply {
            adapter = simpleAdapter
            layoutManager = GridLayoutManager(activity!!, 2)
            hasFixedSize()
        }

    }

    override fun bindView(view: View, item: Any, position: Int) {
        item as Album
        val albumTitle = view.textViewAlbumName
        val albumArtist = view.textViewAlbumArtist
        val artist = item.artistName + " (" + item.songs.size + ")"
        albumTitle.text = item.artistName
        albumArtist.text = artist
    }

    override fun onViewClicked(view: View, item: Any, position: Int) {

    }

    override fun onViewLongClicked(it: View?, item: Any, position: Int) {

    }
}
