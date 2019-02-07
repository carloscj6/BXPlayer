package com.revosleap.bxplayer.utils.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.revosleap.bxplayer.callbacks.BXColor
import com.revosleap.bxplayer.ui.activities.PlayerActivity
import com.revosleap.bxplayer.ui.fragments.*
import org.jetbrains.anko.toast

class MainTabsAdapter(fm: FragmentManager, size: Int, playerActivity: PlayerActivity) : FragmentStatePagerAdapter(fm), BXColor {
    private val faveFrag = FragmentFavorites()
    private val playlist = FragmentPlaylist()
    private val tracks = FragmentTracks()
    private val albums = FragmentAlbum()
    private val artists = FragmentArtists()
    private val player=playerActivity
    override fun songColor(color: Int) {
        tracks.songColor(color)
        albums.songColor(color)
        artists.songColor(color)
    }

    private val fragmentCount = size
    override fun getItem(position: Int): Fragment? {
        player.setColorCallback(this)
        return when (position) {
            0 -> faveFrag
            1 -> playlist
            2 -> tracks
            3 -> albums
            4 -> artists
            else -> null
        }
    }

    override fun getCount(): Int {
        return fragmentCount
    }
}