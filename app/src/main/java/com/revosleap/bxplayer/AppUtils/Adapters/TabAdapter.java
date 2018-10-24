package com.revosleap.bxplayer.AppUtils.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.revosleap.bxplayer.Fragments.FragmentAlbum;
import com.revosleap.bxplayer.Fragments.FragmentArtists;
import com.revosleap.bxplayer.Fragments.FragmentFavorites;
import com.revosleap.bxplayer.Fragments.FragmentPlaylist;
import com.revosleap.bxplayer.Fragments.FragmentTracks;

public class TabAdapter extends FragmentStatePagerAdapter {
    int tabCount;

    public TabAdapter(FragmentManager fm, int tabCount) {
        super(fm);
        this.tabCount = tabCount;
    }
    @Override
    public Fragment getItem(int position){
        switch (position){
            case 0:
                FragmentFavorites favorites= new FragmentFavorites();
                return favorites;
            case 1:
                FragmentPlaylist playlist= new FragmentPlaylist();
                return playlist;
            case 2:
                FragmentTracks tracks= new FragmentTracks();
                return tracks;
            case 3:
                FragmentAlbum album= new FragmentAlbum();
                return album;
            case 4:
                FragmentArtists artists= new FragmentArtists();
                return artists;
                default:
                    return null;
        }
    }


    @Override
    public int getCount() {
        return tabCount;
    }
}
