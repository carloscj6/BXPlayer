package com.revosleap.bxplayer.utils.utils

import android.content.Context
import android.preference.PreferenceManager

class PreferenceHelper(context: Context) {
    companion object {
        private const val SORTING_TOKEN = "player.trackSorting"
        private const val ALBUM_SORTING = "player.albumSorting"
        private const val ARTIST_SORTING = "player.artistSorting"
    }

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    var sortingStyle = preferences.getString(SORTING_TOKEN, "")!!
        set(value) = preferences.edit().putString(SORTING_TOKEN, value).apply()

    var albumSorting = preferences.getString(ALBUM_SORTING, "")!!
        set(value) = preferences.edit().putString(ALBUM_SORTING, value).apply()

    var artistSorting= preferences.getString(ARTIST_SORTING,"")!!
        set(value) = preferences.edit().putString(ARTIST_SORTING,value).apply()

}