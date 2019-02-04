package com.revosleap.bxplayer.ui.viewholders

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.revosleap.bxplayer.R

class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var trackImage: ImageView = itemView.findViewById(R.id.imageView2)
    var title: TextView
    var artist: TextView

    init {
        title = itemView.findViewById(R.id.textViewTitleTrack)
        artist = itemView.findViewById(R.id.textViewArtistTrack)

    }
}
