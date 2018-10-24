package com.revosleap.bxplayer.AppUtils.Utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.revosleap.bxplayer.AppUtils.Models.Album;
import com.revosleap.bxplayer.AppUtils.Models.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SongProvider {
    private static final int TITLE = 0;
    private static final int TRACK = 1;
    private static final int YEAR = 2;
    private static final int DURATION = 3;
    private static final int PATH = 4;
    private static final int ALBUM = 5;
    private static final int ARTIST_ID = 6;
    private static final int ARTIST = 7;

    private static final String[] BASE_PROJECTION = new String[]{
            MediaStore.Audio.AudioColumns.TITLE,// 0
            MediaStore.Audio.AudioColumns.TRACK,// 1
            MediaStore.Audio.AudioColumns.YEAR,// 2
            MediaStore.Audio.AudioColumns.DURATION,// 3
            MediaStore.Audio.AudioColumns.DATA,// 4
            MediaStore.Audio.AudioColumns.ALBUM,// 5
            MediaStore.Audio.AudioColumns.ARTIST_ID,// 6
            MediaStore.Audio.AudioColumns.ARTIST,// 7
    };

    private static List<Song> mAllDeviceSongs = new ArrayList<>();

    public static List<Song> getAllDeviceSongs() {
        return mAllDeviceSongs;
    }

    public static List<Song> getAllArtistSongs(@NonNull final List<Album> albums) {
        final List<Song> songsList = new ArrayList<>();
        for (Album album : albums) {
            songsList.addAll(album.songs);
        }
        return songsList;
    }

    @NonNull
    static List<Song> getSongs(@Nullable final Cursor cursor) {
        final List<Song> songs = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final Song song = getSongFromCursorImpl(cursor);
                if (song.duration >= 5000) {
                    songs.add(song);
                    mAllDeviceSongs.add(song);
                }
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        if (songs.size() > 1) {
            sortSongsByTrack(songs);
        }
        return songs;
    }

    private static void sortSongsByTrack(List<Song> songs) {

        Collections.sort(songs, new Comparator<Song>() {
            public int compare(Song obj1, Song obj2) {
                return Long.compare(obj1.trackNumber, obj2.trackNumber);
            }
        });
    }

    @NonNull
    private static Song getSongFromCursorImpl(@NonNull Cursor cursor) {
        final String title = cursor.getString(TITLE);
        final int trackNumber = cursor.getInt(TRACK);
        final int year = cursor.getInt(YEAR);
        final int duration = cursor.getInt(DURATION);
        final String uri = cursor.getString(PATH);
        final String albumName = cursor.getString(ALBUM);
        final int artistId = cursor.getInt(ARTIST_ID);
        final String artistName = cursor.getString(ARTIST);

        return new Song(title, trackNumber, year, duration, uri, albumName, artistId, artistName);
    }

    @Nullable
    static Cursor makeSongCursor(@NonNull final Context context, final String sortOrder) {
        try {
            return context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    BASE_PROJECTION, null, null, sortOrder);
        } catch (SecurityException e) {
            return null;
        }
    }
}
