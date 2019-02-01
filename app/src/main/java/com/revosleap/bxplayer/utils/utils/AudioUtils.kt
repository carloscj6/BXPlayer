package com.revosleap.bxplayer.utils.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.Locale
import java.util.concurrent.TimeUnit

object AudioUtils {
    fun cover(path: String): Bitmap {
        val retriever = MediaMetadataRetriever()
        var inputStream: InputStream? = null
        retriever.setDataSource(path)
        if (retriever.embeddedPicture != null) {
            inputStream = ByteArrayInputStream(retriever.embeddedPicture)


        }
        val image = BitmapFactory.decodeStream(inputStream)
        retriever.release()
        return image
    }

    fun formatDuration(duration: Int): String {
        return String.format(Locale.getDefault(), "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration.toLong())))
    }

}
