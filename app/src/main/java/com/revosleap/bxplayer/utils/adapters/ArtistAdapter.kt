package com.revosleap.bxplayer.utils.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.revosleap.bxplayer.R
import com.revosleap.bxplayer.ui.viewholders.TrackViewHolder
import com.revosleap.bxplayer.models.AudioModel

class ArtistAdapter(private var artistList: MutableList<AudioModel>, internal var context: Context) : RecyclerView.Adapter<TrackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.track, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val list = artistList[position]
        val artist = list.artist
        val track = list.title

        holder.title.text = artist
        holder.artist.text = track
    }

    override fun getItemCount(): Int {
        return artistList.size
    }
}
