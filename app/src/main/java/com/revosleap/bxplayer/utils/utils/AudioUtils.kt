package com.revosleap.bxplayer.utils.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import com.revosleap.bxplayer.R
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.*
import java.util.concurrent.TimeUnit

object AudioUtils {
    @Throws(IllegalStateException::class)
    fun cover(path: String, context: Context): Bitmap {
        val retriever = MediaMetadataRetriever()
        val inputStream: InputStream?
        retriever.setDataSource(path)
        return if (retriever.embeddedPicture != null) {
            inputStream = ByteArrayInputStream(retriever.embeddedPicture)
            val image = BitmapFactory.decodeStream(inputStream)
            retriever.release()
            image
        } else {
            BitmapFactory.decodeResource(context.resources,
                    R.drawable.cover2)
        }
    }

    fun formatDuration(duration: Int): String {
        return String.format(Locale.getDefault(), "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration.toLong())))
    }

}
