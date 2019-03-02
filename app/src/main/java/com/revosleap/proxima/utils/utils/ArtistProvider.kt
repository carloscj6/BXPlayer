package com.revosleap.proxima.utils.utils

import android.content.Context
import android.provider.MediaStore
import com.revosleap.proxima.models.Album
import com.revosleap.proxima.models.Artist
import java.util.*

object ArtistProvider {
    val ARTISTS_LOADER = 0

    private val songLoaderSortOrder: String
        get() = MediaStore.Audio.Artists.DEFAULT_SORT_ORDER + ", " + MediaStore.Audio.Albums.DEFAULT_SORT_ORDER + ", " + MediaStore.Audio.Media.DEFAULT_SORT_ORDER

    @Throws(Exception::class)
    private fun sortArtists(artists: MutableList<Artist>) {
        artists.sortWith(Comparator { obj1, obj2 -> obj1.name?.compareTo(obj2.name!!, ignoreCase = true)!! })
    }

    @Throws(Exception::class)
    fun getAllArtists(context: Context): MutableList<Artist> {
        val songs = SongProvider.getAllDeviceSongs(context)
//  sortArtists(artists);
        return retrieveArtists(AlbumProvider.retrieveAlbums(songs))
    }

    fun getArtist(artists: MutableList<Artist>, selectedArtist: String): Artist? {
        var returnerArtist: Artist? = null
        for (artist in artists) {
            if (artist.name == selectedArtist) {
                returnerArtist = artist
            }
        }
        return returnerArtist
    }

    private fun retrieveArtists(albums: MutableList<Album>?): MutableList<Artist> {
        val artists = mutableListOf<Artist>()
        if (albums != null) {
            for (album in albums) {
                getOrCreateArtist(artists, album.artistId).albums.add(album)
            }
        }
        return artists
    }

    private fun getOrCreateArtist(artists: MutableList<Artist>, artistId: Int): Artist {
        for (artist in artists) {
            if (!artist.albums.isEmpty() && !artist.albums[0].songs.isEmpty()
                    && artist.albums[0].songs[0].artistId == artistId) {
                return artist
            }
        }
        val artist = Artist()
        artists.add(artist)
        return artist
    }

    internal class AsyncArtistLoader(context: Context) : WrappedAsyncTaskLoader<MutableList<Artist>>(context) {

        override fun loadInBackground(): MutableList<Artist>? {
            try {
                return getAllArtists(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }
    }
}
