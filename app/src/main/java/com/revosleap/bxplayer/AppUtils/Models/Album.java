package com.revosleap.bxplayer.AppUtils.Models;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Album {

    public final List<AudioModel> songs;

    public Album() {
        this.songs = new ArrayList<>();
    }

    public String getTitle() {
        return getFirstSong().getAlbum();
    }

    public final int getArtistId() {
        return getFirstSong().getArtistId();
    }

    public final String getArtistName() {
        return getFirstSong().getArtist();
    }

    public final int getYear() {
        return getFirstSong().getSongYear();
    }

    public final int getSongCount() {
        return songs.size();
    }

    @NonNull
    private AudioModel getFirstSong() {
       // return songs.isEmpty() ? AudioModel.EMPTY_SONG : songs.get(0);
        return null;
    }
}
