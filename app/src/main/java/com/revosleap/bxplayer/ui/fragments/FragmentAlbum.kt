package com.revosleap.bxplayer.ui.fragments


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import com.google.gson.Gson
import com.revosleap.bxplayer.R
import com.revosleap.bxplayer.callbacks.BXColor
import com.revosleap.bxplayer.models.Album
import com.revosleap.bxplayer.models.Song
import com.revosleap.bxplayer.ui.activities.PlayerActivity
import com.revosleap.bxplayer.utils.utils.AlbumProvider
import com.revosleap.bxplayer.utils.utils.PreferenceHelper
import com.revosleap.bxplayer.utils.utils.SongProvider
import com.revosleap.bxplayer.utils.utils.Universal
import com.revosleap.simpleadapter.SimpleAdapter
import com.revosleap.simpleadapter.SimpleCallbacks
import kotlinx.android.synthetic.main.album_item.view.*
import kotlinx.android.synthetic.main.fragment_fragment_album.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info


class FragmentAlbum : Fragment(), SimpleCallbacks, AnkoLogger, BXColor {
    private var musicList = mutableListOf<Song>()
    private var albumList = mutableListOf<Album>()
    var simpleAdapter: SimpleAdapter? = null
    private var preferenceHelper: PreferenceHelper? = null
    private var playerActivity: PlayerActivity? = null
    private var viewColor = 0

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        playerActivity = activity!! as PlayerActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        simpleAdapter = SimpleAdapter(R.layout.album_item, this)
        musicList = SongProvider.getAllDeviceSongs(activity!!)
        info(musicList.size)
        preferenceHelper = PreferenceHelper(activity!!)
        return inflater.inflate(R.layout.fragment_fragment_album, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getAlbumList()
        simpleAdapter?.setHasStableIds(true)
        recyclerViewAlbum.apply {
            adapter = simpleAdapter
            layoutManager = GridLayoutManager(activity!!, 2)
            hasFixedSize()
        }
        buttonListSortAlbums.setOnClickListener {
            sortAlbums(it)
        }
        if (viewColor != 0) {
            buttonListSortAlbums?.setColorFilter(viewColor)
        }
    }

    override fun bindView(view: View, item: Any, position: Int) {
        item as Album
        val albumTitle = view.textViewAlbumName
        val albumArtist = view.textViewAlbumArtist
        val artist = item.artistName + " (" + item.songs.size + ")"
        albumTitle.text = item.title
        albumArtist.text = artist
    }

    override fun onViewClicked(view: View, item: Any, position: Int) {
        goToInfo(position)
    }

    override fun onViewLongClicked(it: View?, item: Any, position: Int) {

    }

    override fun songColor(color: Int) {
        viewColor = color

    }

    private fun goToInfo(position: Int) {
        val gson = Gson()
        val gsonString = gson.toJson(albumList[position].songs)
        val albumInfo = FragmentAlbumInfo()
        val bundle = Bundle()
        bundle.putString(Universal.ALBUM_BUNDLE, gsonString)
        albumInfo.arguments = bundle
        playerActivity?.supportFragmentManager!!
                .beginTransaction()
                .replace(R.id.frame_current, albumInfo, Universal.ALBUM_INFO_TAG)
                .addToBackStack(null)
                .commit()
        playerActivity?.replaceFragment()
    }

    private fun sortAlbums(view: View) {
        val menu = PopupMenu(activity!!, view)
        menu.inflate(R.menu.album_sorting)
        menu.show()
        menu.setOnMenuItemClickListener { item ->
            when (item?.itemId) {
                R.id.item_artist -> {
                    preferenceHelper?.sortingStyle = Universal.SORT_BY_ARTIST
                    val sorted = albumList.sortedWith(compareBy { it.artistName })
                    simpleAdapter?.changeItems(sorted.toMutableList())
                    albumList = sorted.toMutableList()
                }
                R.id.item_release -> {
                    preferenceHelper?.sortingStyle = Universal.SORT_BY_YEAR
                    val sorted = albumList.sortedWith(compareBy { it.year })
                    simpleAdapter?.changeItems(sorted.toMutableList())
                    albumList = sorted.toMutableList()
                }
                R.id.item_title -> {
                    preferenceHelper?.sortingStyle = Universal.SORT_BY_TITLE
                    val sorted = albumList.sortedWith(compareBy { it.title })
                    simpleAdapter?.changeItems(sorted.toMutableList())
                    albumList = sorted.toMutableList()
                }

            }
            true
        }
    }

    private fun getAlbumList() {
        val sortOrder = preferenceHelper?.albumSorting
        albumList = AlbumProvider.retrieveAlbums(musicList)
        if (sortOrder!!.isEmpty()) {
            simpleAdapter?.addManyItems(albumList.toMutableList())

        }
        if (sortOrder == Universal.SORT_BY_ARTIST) {
            val sorted = albumList.sortedWith(compareBy { it.artistName })
            simpleAdapter?.changeItems(sorted.toMutableList())
            albumList = sorted.toMutableList()
        }
        if (sortOrder == Universal.SORT_BY_TITLE) {
            val sorted = albumList.sortedWith(compareBy { it.title })
            simpleAdapter?.changeItems(sorted.toMutableList())
            albumList = sorted.toMutableList()
        }
        if (sortOrder == Universal.SORT_BY_YEAR) {
            val dateSorted = albumList.sortedWith(compareBy { it.year })
            simpleAdapter?.changeItems(dateSorted.toMutableList())
            albumList = dateSorted.toMutableList()
        }
    }
}
