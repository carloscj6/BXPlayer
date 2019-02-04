package com.revosleap.bxplayer.utils.utils

import com.revosleap.bxplayer.models.Album
import com.revosleap.bxplayer.models.Song
import java.util.*

object AlbumProvider {
    fun retrieveAlbums(songs: MutableList<Song>?): MutableList<Album> {
        val albums = mutableListOf<Album>()
        if (songs != null) {
            for (song in songs) {
                getAlbum(albums, song.albumName!!).songs.add(song)
            }
        }
        if (albums.size > 1) {
            sortAlbums(albums)
        }
        return albums
    }

    private fun sortAlbums(albums: MutableList<Album>) {
        albums.sortWith(Comparator { obj1, obj2 -> Integer.compare(obj1.year, obj2.year) })
    }

    private fun getAlbum(albums: MutableList<Album>, albumName: String): Album {
        for (album in albums) {
            if (!album.songs.isEmpty() && album.songs[0].albumName == albumName) {
                return album
            }
        }
        val album = Album()
        albums.add(album)
        return album
    }
}
