package com.revosleap.bxplayer.models

class Album {
    val songs: MutableList<Song> = mutableListOf()

    val title: String
        get() = firstSong.albumName!!

    val artistId: Int
        get() = firstSong.artistId

    val artistName: String?
        get() = firstSong.artistName

    val year: Int
        get() = firstSong.year

    val songCount: Int
        get() = songs.size

    private val firstSong: Song
        get() = if (songs.isEmpty()) Song.EMPTY_SONG else songs[0]

}
