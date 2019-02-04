package com.revosleap.bxplayer.utils.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.revosleap.bxplayer.ui.fragments.*

class TabFragmentAdapter(fm: FragmentManager,size:Int): FragmentStatePagerAdapter(fm) {
    private val fragmentCount= size
    override fun getItem(position: Int): Fragment? {
        return when(position){
            0-> FragmentFavorites()
            1-> FragmentPlaylist()
            2-> FragmentTracks()
            3-> FragmentAlbum()
            4-> FragmentArtists()
            else-> null
        }
    }

    override fun getCount(): Int {
      return fragmentCount
    }
}