package com.revosleap.bxplayer.models

import java.util.ArrayList

class Album {

    private val songs: List<AudioModel>

    val title: String?
        get() = firstSong?.album

    val artistId: Int
        get() = firstSong?.artistId!!

    val artistName: String?
        get() = firstSong?.artist

    val year: Int
        get() = firstSong?.songYear!!

    val songCount: Int
        get() = songs.size

    private// return songs.isEmpty() ? AudioModel.EMPTY_SONG : songs.get(0);
    val firstSong: AudioModel?
        get() = null

    init {
        this.songs = ArrayList()
    }
}
