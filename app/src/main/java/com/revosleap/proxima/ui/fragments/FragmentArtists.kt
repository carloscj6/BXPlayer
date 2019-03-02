package com.revosleap.proxima.ui.fragments


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import com.google.gson.Gson
import com.revosleap.proxima.R
import com.revosleap.proxima.callbacks.BXColor
import com.revosleap.proxima.models.Artist
import com.revosleap.proxima.models.Song
import com.revosleap.proxima.ui.activities.PlayerActivity
import com.revosleap.proxima.utils.utils.ArtistProvider
import com.revosleap.proxima.utils.utils.PreferenceHelper
import com.revosleap.proxima.utils.utils.Universal
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
    private var playerActivity: PlayerActivity? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        playerActivity = activity as PlayerActivity
    }

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
        viewDetails(position)
    }

    override fun onViewLongClicked(it: View?, item: Any, position: Int) {

    }

    override fun songColor(color: Int) {
        viewColors = color

    }

    private fun viewDetails(position: Int) {
        val fragmentArtistInfo = FragmentArtistInfo()
        val artist = artistList[position]
        val albums = artist.albums
        val songs = mutableListOf<Song>()
        albums.forEach {
            it.songs.forEach { song ->
                songs.add(song)
            }
        }
        val gson = Gson()
        val albumString = gson.toJson(albums)
        val songString = gson.toJson(songs)
        val bundle = Bundle()
        bundle.putString(Universal.ALBUMS_BUNDLE, albumString)
        bundle.putString(Universal.SONGS_BUNDLE, songString)
        fragmentArtistInfo.arguments = bundle
        playerActivity?.supportFragmentManager!!
                .beginTransaction()
                .replace(R.id.frame_current, fragmentArtistInfo, Universal.ALBUM_INFO_TAG)
                .addToBackStack(null)
                .commit()
        playerActivity?.replaceFragment()

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
