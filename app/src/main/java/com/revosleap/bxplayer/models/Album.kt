package com.revosleap.bxplayer.models

import java.io.Serializable

class Album:Serializable {
    val songs: MutableList<Song> = mutableListOf()

    val title: String
        get() = firstSong.albumName!!

    val artistId: Int
        get() = firstSong.artistId

    val artistName: String?
        get() = firstSong.artist

    val year: Int
        get() = firstSong.songYear

    val songCount: Int
        get() = songs.size

    private val firstSong: Song
        get() = songs[0]

}
