package com.revosleap.bxplayer.ui.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import com.revosleap.bxplayer.R
import com.revosleap.bxplayer.callbacks.BXColor
import com.revosleap.bxplayer.models.Artist
import com.revosleap.bxplayer.utils.utils.ArtistProvider
import com.revosleap.bxplayer.utils.utils.PreferenceHelper
import com.revosleap.bxplayer.utils.utils.Universal
import com.revosleap.simpleadapter.SimpleAdapter
import com.revosleap.simpleadapter.SimpleCallbacks
import kotlinx.android.synthetic.main.artist_item.view.*
import kotlinx.android.synthetic.main.fragment_artists.*
import org.jetbrains.anko.AnkoLogger


class FragmentArtists : Fragment(), SimpleCallbacks, AnkoLogger, BXColor {
    private var preferenceHelper: PreferenceHelper? = null
    private var simpleAdapter: SimpleAdapter? = null
    private var artistList = mutableListOf<Artist>()
    private var viewColors = 0
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        simpleAdapter = SimpleAdapter(R.layout.artist_item, this)
        artistList = ArtistProvider.getAllArtists(activity!!)
        preferenceHelper = PreferenceHelper(activity!!)
        return inflater.inflate(R.layout.fragment_artists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getSongs()
        artistrecycler.apply {
            adapter = simpleAdapter
            layoutManager = LinearLayoutManager(activity)
            hasFixedSize()
        }
        buttonListSortArtists.setOnClickListener {
            sortArtists(it)
        }
        if (viewColors != 0) {
            buttonListSortArtists?.setColorFilter(viewColors)
        }
    }

    override fun bindView(view: View, item: Any, position: Int) {
        item as Artist
        val artistName = view.textViewArtistName
        val artistInfo = view.textViewArtistInfo
        artistName.text = item.name
        var album = "Album"
        var track = "Track"
        if (item.albums.size > 1) {
            album = "Albums"
        }
        if (item.songCount > 1) {
            track = "Tracks"
        }
        val info = item.songCount.toString() + " $track | " + item.albums.size + " $album"
        artistInfo.text = info
    }

    override fun onViewClicked(view: View, item: Any, position: Int) {

    }

    override fun onViewLongClicked(it: View?, item: Any, position: Int) {

    }

    override fun songColor(color: Int) {
        viewColors = color

    }

    private fun sortArtists(view: View) {
        val menu = PopupMenu(activity!!, view)
        menu.inflate(R.menu.artist_sorting)
        menu.show()
        menu.setOnMenuItemClickListener { item ->
            when (item?.itemId) {
                R.id.item_name -> {
                    preferenceHelper?.artistSorting = Universal.SORT_BY_NAME
                    val sorted = artistList.sortedWith(compareBy { it.name })
                    artistList = sorted.toMutableList()
                    simpleAdapter?.changeItems(artistList.toMutableList())
                }
                R.id.item_songs -> {
                    preferenceHelper?.artistSorting = Universal.SORT_BY_SONGS
                    val sorted = artistList.sortedWith(compareBy { it.songCount })
                    artistList = sorted.toMutableList()
                    artistList.reverse()
                    simpleAdapter?.changeItems(artistList.toMutableList())
                }
            }
            true
        }
    }

    private fun getSongs() {
        val sorting = preferenceHelper?.artistSorting
        if (sorting?.isEmpty()!!) {
            simpleAdapter?.addManyItems(artistList.toMutableList())
        }
        if (sorting == Universal.SORT_BY_SONGS) {
            val sorted = artistList.sortedWith(compareBy { it.songCount })
            artistList = sorted.toMutableList()
            artistList.reverse()
            simpleAdapter?.changeItems(artistList.toMutableList())
        }
        if (sorting == Universal.SORT_BY_NAME) {
            val sorted = artistList.sortedWith(compareBy { it.name })
            artistList = sorted.toMutableList()
            simpleAdapter?.changeItems(artistList.toMutableList())
        }
    }
}
