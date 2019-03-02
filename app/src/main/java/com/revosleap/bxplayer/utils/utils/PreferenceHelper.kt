package com.revosleap.bxplayer.utils.utils

import android.content.Context
import android.preference.PreferenceManager

class PreferenceHelper(context: Context) {
    companion object {
        private const val SORTING_TOKEN = "player.trackSorting"
        private const val ALBUM_SORTING = "player.albumSorting"
        private const val ARTIST_SORTING = "player.artistSorting"
        private const val CURRENT_PLAYING = "player.currentPlaying"
        private const val CURRENT_PLAYING_INDEX = "player.currentPlayingIndex"
        private const val CURRENT_PLAYING_POSITION="current.song.position"
    }

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    var sortingStyle = preferences.getString(SORTING_TOKEN, "")!!
        set(value) = preferences.edit().putString(SORTING_TOKEN, value).apply()

    var albumSorting = preferences.getString(ALBUM_SORTING, "")!!
        set(value) = preferences.edit().putString(ALBUM_SORTING, value).apply()

    var artistSorting = preferences.getString(ARTIST_SORTING, "")!!
        set(value) = preferences.edit().putString(ARTIST_SORTING, value).apply()

    var playingList = preferences.getString(CURRENT_PLAYING, "")!!
        set(value) = preferences.edit().putString(CURRENT_PLAYING, value).apply()

    var currentIndex = preferences.getInt(CURRENT_PLAYING_INDEX, 0)
        set(value) = preferences.edit().putInt(CURRENT_PLAYING_INDEX, value).apply()

    var currentPosition = preferences.getInt(CURRENT_PLAYING_INDEX, 0)
        set(value) = preferences.edit().putInt(CURRENT_PLAYING_POSITION, value).apply()

}