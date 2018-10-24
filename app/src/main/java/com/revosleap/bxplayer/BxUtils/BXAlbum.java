package com.revosleap.bxplayer.BxUtils;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class BXAlbum {
    public final List<BXCurrentSong> songs;

    public BXAlbum() {
        this.songs = new ArrayList<>();
    }

    public String getTitle() {
        return getFirstSong().albumName;
    }

    public final int getArtistId() {
        return getFirstSong().artistId;
    }

    public final String getArtistName() {
        return getFirstSong().artistName;
    }

    public final int getYear() {
        return getFirstSong().year;
    }

    public final int getSongCount() {
        return songs.size();
    }

    @NonNull
    private BXCurrentSong getFirstSong() {
        return songs.isEmpty() ? BXCurrentSong.EMPTY_SONG : songs.get(0);
    }
}
