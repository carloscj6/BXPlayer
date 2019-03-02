//package com.revosleap.bxplayer.utils.utils
//
//import android.content.Context
//import android.database.Cursor
//import android.provider.MediaStore
//import com.revosleap.bxplayer.models.Album
//import com.revosleap.bxplayer.models.PlayList
//
//
//object PlayListProvider {
//    private const val AUDIO_ID= 0
//    private const val CONTENT_DIRECTORY= 1
//    private const val PLAYLIST_ID= 2
//    private const val _ID = 3
//
//
//    private val BASE_PROJECTION = arrayOf(
//            MediaStore.Audio.PlaylistsColumns.NAME,
//            MediaStore.Audio.PlaylistsColumns.DATA,
//            MediaStore.Audio.PlaylistsColumns.DATE_ADDED
//    )
//    fun getAllPlayLists(cursor: Cursor?):MutableList<PlayList>{
//    val playLists= mutableListOf<PlayList>()
//        if (cursor!=null && cursor.moveToFirst()){
//
//        }
//    }
//    private fun getPlayListFromCursor(cursor: Cursor):Album{
//       val album= Album()
//        album.title =
//    }
//    private fun makePlaylistCursor(context: Context, sortOrder: String?): Cursor? {
//        try {
//            return context.contentResolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
//                    BASE_PROJECTION, null, null, sortOrder)
//        } catch (e: SecurityException) {
//            return null
//        }
//
//    }
//}