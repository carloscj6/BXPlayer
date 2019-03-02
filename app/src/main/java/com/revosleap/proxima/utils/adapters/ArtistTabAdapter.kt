package com.revosleap.proxima.utils.adapters

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.revosleap.proxima.ui.fragments.FragmentArtistAlbum
import com.revosleap.proxima.ui.fragments.FragmentArtistTrack
import com.revosleap.proxima.utils.utils.Universal

class ArtistTabAdapter(fm: FragmentManager,songString: String?,albumString: String?)
    : FragmentStatePagerAdapter(fm) {
    val album= albumString
    val song= songString
    val bundle = Bundle()
    private val fragmentArtistAlbum = FragmentArtistAlbum()
    private val fragmentArtistTrack = FragmentArtistTrack()

    override fun getItem(position: Int): Fragment? {
        bundle.putString(Universal.SONGS_BUNDLE,song)
        bundle.putString(Universal.ALBUMS_BUNDLE,album)
        return when (position) {
            0 -> {
                fragmentArtistTrack.arguments = bundle
                fragmentArtistTrack
            }
            1 -> {
                fragmentArtistAlbum.arguments= bundle
                fragmentArtistAlbum
            }
            else -> null
        }

    }

    override fun getCount(): Int {
        return 2
    }
}