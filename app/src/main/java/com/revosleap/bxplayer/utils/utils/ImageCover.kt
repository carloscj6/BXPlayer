package com.revosleap.bxplayer.utils.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.AsyncTask
import android.widget.ImageView

import java.io.ByteArrayInputStream
import java.io.InputStream

class ImageCover(imageView: ImageView, path: String) : AsyncTask<String, Void, Bitmap>() {
    private val coverImage = imageView
    private val imagePath = path
    override fun doInBackground(vararg strings: String): Bitmap {
        val retriever = MediaMetadataRetriever()
        var inputStream: InputStream? = null
        retriever.setDataSource(imagePath)

        if (retriever.embeddedPicture != null) {
            inputStream = ByteArrayInputStream(retriever.embeddedPicture)


        }
        val image = BitmapFactory.decodeStream(inputStream)
        retriever.release()
        return image
    }

    override fun onPostExecute(result: Bitmap?) {
        super.onPostExecute(result)
        coverImage.setImageBitmap(result)
    }
}
