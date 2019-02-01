package com.revosleap.bxplayer.utils.utils

import android.content.Context
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.revosleap.bxplayer.utils.models.AudioModel
import java.util.*

class GetAudio {
    fun geAllAudio(context: Context): List<AudioModel> {

        val temAudioList = ArrayList<AudioModel>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val order = MediaStore.Audio.Media.TITLE

        val projection = arrayOf(MediaStore.Audio.AudioColumns.DATA, MediaStore.Audio.AudioColumns.ALBUM, MediaStore.Audio.ArtistColumns.ARTIST, MediaStore.Audio.AudioColumns.TITLE, MediaStore.Audio.AudioColumns.DURATION)
        val cursor = context.contentResolver.query(uri, projection, null, null, order)
        if (cursor != null) {
            Log.v("Count", "Songs " + cursor.count)
            while (cursor.moveToNext()) {

                val model = AudioModel()
                val path = cursor.getString(0)
                val album = cursor.getString(1)
                val artist = cursor.getString(2)
                val name = cursor.getString(3)
                val duration = cursor.getString(4)

                try {
                    model.title = name

                    model.album = album
                    model.artist = artist
                    model.path = path
                    model.duration = Integer.parseInt(duration)
                } catch (e: Exception) {
                    e.printStackTrace()
                }


                temAudioList.add(model)
            }
            cursor.close()
        } else
            Toast.makeText(context, "Null", Toast.LENGTH_SHORT).show()
        return temAudioList

    }
}
