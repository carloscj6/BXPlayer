package com.revosleap.bxplayer.ui.fragments

import android.content.Context
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.revosleap.bxplayer.R
import com.revosleap.bxplayer.models.Album
import com.revosleap.bxplayer.models.Song
import com.revosleap.bxplayer.ui.activities.PlayerActivity
import com.revosleap.bxplayer.utils.adapters.ArtistTabAdapter
import com.revosleap.bxplayer.utils.utils.Universal
import kotlinx.android.synthetic.main.fragment_artist_info.*
import java.lang.reflect.Type

class FragmentArtistInfo : Fragment() {
    private var playerActivity: PlayerActivity? = null
    private var artistTabAdapter: ArtistTabAdapter? = null
    private var songs = mutableListOf<Song>()
    private var albums = mutableListOf<Album>()
    private var songString: String? = null
    private var albumString: String? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        playerActivity = activity as PlayerActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            : View? {
        setHasOptionsMenu(true)
        getItems()
        return inflater.inflate(R.layout.fragment_artist_info, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTabs()
        val params = linearLayout.layoutParams as ViewGroup.MarginLayoutParams
        params.bottomMargin = playerActivity?.controls()!!.height
        linearLayout.layoutParams = params
    }


    private fun setTabs() {
        artistTabAdapter = ArtistTabAdapter(playerActivity?.supportFragmentManager!!, songString, albumString)
        tabLayoutArtist.apply {
            addTab(newTab().setText("Tracks (${songs.size})"))
            addTab(newTab().setText("Albums (${albums.size})"))
            tabGravity = TabLayout.GRAVITY_FILL
        }
        viewPagerArtist.apply {
            adapter = artistTabAdapter
            addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayoutArtist))
        }
        tabLayoutArtist.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(viewPagerArtist))
    }

    private fun getItems() {
        songString = arguments?.getString(Universal.SONGS_BUNDLE)
        albumString = arguments?.getString(Universal.ALBUMS_BUNDLE)
        val gson = Gson()
        val type: Type = object : TypeToken<MutableList<Song>>() {}.type
        val albumType: Type = object : TypeToken<MutableList<Album>>() {}.type
        val songs = gson.fromJson<MutableList<Song>>(songString, type)
        val artAlbums = gson.fromJson<MutableList<Album>>(albumString, albumType)
        if (songs != null && songs.size > 0 && artAlbums != null && artAlbums.size > 0) {
            this.songs = songs
            albums = artAlbums
        }


    }
}