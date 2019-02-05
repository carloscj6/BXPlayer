package com.revosleap.bxplayer.utils.utils

import android.content.Context
import android.preference.PreferenceManager

class PreferenceHelper(context:Context) {
    companion object {
        private const val SORTING_TOKEN = "player.trackSorting"
    }
    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    var sortingStyle = preferences.getString(SORTING_TOKEN, "")!!
        set(value) = preferences.edit().putString(SORTING_TOKEN,     value).apply()

}