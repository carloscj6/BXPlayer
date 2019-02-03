package com.revosleap.bxplayer.ui.viewholders

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.revosleap.bxplayer.R

class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var trackImage: ImageView
    var title: TextView
    var artist: TextView

    init {
        trackImage = itemView.findViewById(R.id.imageView2)
        title = itemView.findViewById(R.id.textViewTitleTrack)
        artist = itemView.findViewById(R.id.textViewArtistTrack)

    }
}
