package com.revosleap.bxplayer.utils.utils

object UniversalUtils {
    fun formatTime(ms: Long): String {
        val totalSecs = ms / 1000
        val hours = totalSecs / 3600
        val minutes = totalSecs / 60 % 60
        val secs = totalSecs % 60
        val minutesString = when {
            minutes == 0L -> "00"
            minutes < 10 -> "0$minutes"
            else -> "" + minutes
        }
        val secsString = when {
            secs == 0L -> "00"
            secs < 10 -> "0$secs"
            else -> "" + secs
        }
        return when {
            hours > 0 -> "$hours:$minutesString:$secsString"
            minutes > 0 -> "$minutes:$secsString"
            else -> "0:$secsString"
        }
    }

    fun formatTrack(trackNumber: Int): Int {
        var formatted = trackNumber
        if (trackNumber >= 1000) {
            formatted = trackNumber % 1000
        }
        return formatted
    }
}