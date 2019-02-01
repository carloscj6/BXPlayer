package com.revosleap.bxplayer.utils.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.revosleap.bxplayer.ui.fragments.*

class TabAdapter(fm: FragmentManager, private var tabCount: Int) : FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): Fragment? {
        when (position) {
            0 -> {
                return FragmentFavorites()
            }
            1 -> {
                return FragmentPlaylist()
            }
            2 -> {
                return FragmentTracks()
            }
            3 -> {
                return FragmentAlbum()
            }
            4 -> {
                return FragmentArtists()
            }
            else -> return null
        }
    }


    override fun getCount(): Int {
        return tabCount
    }
}
