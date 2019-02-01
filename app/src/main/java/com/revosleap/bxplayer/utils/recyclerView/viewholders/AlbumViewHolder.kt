package com.revosleap.bxplayer.utils.recyclerView.viewholders

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.revosleap.bxplayer.R

class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var albumCover: ImageView
    var album: TextView
    var albumArtist: TextView

    init {
        albumCover = itemView.findViewById(R.id.imageViewAlbum)
        album = itemView.findViewById(R.id.textViewAlbumName)
        albumArtist = itemView.findViewById(R.id.textViewAlbumArtist)
    }
}
