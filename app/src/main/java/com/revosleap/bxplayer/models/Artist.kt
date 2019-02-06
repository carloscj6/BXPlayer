package com.revosleap.bxplayer.models

import java.io.Serializable

class Artist:Serializable {
    val albums: MutableList<Album> = mutableListOf()

    val id: Int
        get() = firstAlbum.artistId

    val name: String?
        get() = firstAlbum.artistName

    private val firstAlbum: Album
        get() = if (albums.isEmpty()) Album() else albums[0]

    val songCount: Int
        get() {
            var songCount = 0
            for (album in albums) {
                songCount += album.songCount
            }
            return songCount
        }

}
