package com.revosleap.proxima.utils.utils

import android.content.Context
import android.provider.MediaStore
import com.revosleap.proxima.models.Song
import org.jetbrains.anko.toast
import java.util.*

class GetAudio {
    fun geAllAudio(context: Context): MutableList<Song> {

        val temAudioList = ArrayList<Song>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val order = MediaStore.Audio.Media.TITLE

        val projection = arrayOf(MediaStore.Audio.AudioColumns.DATA, MediaStore.Audio.AudioColumns.ALBUM, MediaStore.Audio.ArtistColumns.ARTIST, MediaStore.Audio.AudioColumns.TITLE, MediaStore.Audio.AudioColumns.DURATION)
        val cursor = context.contentResolver.query(uri, projection, null, null, order)
        if (cursor != null) {
            while (cursor.moveToNext()) {

                val model = Song()
                val path = cursor.getString(0)
                val album = cursor.getString(1)
                val artist = cursor.getString(2)
                val name = cursor.getString(3)
                val duration = cursor.getString(4)

                try {
                    model.title = name
                    model.albumName = album
                    model.artist = artist
                    model.path = path
                    model.duration = duration.toLong()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if(duration.toLong() >= 30000){
                    temAudioList.add(model)
                }

            }
            cursor.close()
        } else
            context.toast("No Audio Files Found")
        return temAudioList

    }
}
