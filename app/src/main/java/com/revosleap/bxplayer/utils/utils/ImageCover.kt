package com.revosleap.bxplayer.utils.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.AsyncTask

import java.io.ByteArrayInputStream
import java.io.InputStream

class ImageCover : AsyncTask<String, Void, Bitmap>() {

    override fun doInBackground(vararg strings: String): Bitmap {
        val retriever = MediaMetadataRetriever()
        var inputStream: InputStream? = null
        retriever.setDataSource(strings[0])

        if (retriever.embeddedPicture != null) {
            inputStream = ByteArrayInputStream(retriever.embeddedPicture)


        }
        val image = BitmapFactory.decodeStream(inputStream)
        retriever.release()
        return image
    }
}
