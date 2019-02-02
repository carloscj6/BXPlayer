package com.revosleap.bxplayer.utils.adapters

import android.app.Activity
import android.media.MediaMetadataRetriever
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.revosleap.bxplayer.R
import com.revosleap.bxplayer.utils.models.AudioModel
import java.io.ByteArrayInputStream
import java.io.InputStream

class TrackAdapter(internal var tracklist: MutableList<AudioModel>, private val mActivity: Activity, private val mSongSelectedListener: SongSelectedListener) : RecyclerView.Adapter<TrackAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val model = tracklist[position]
        val title = model.title
        val artist = model.artist
        val path = model.path
        holder.artist.text = title
        holder.title.text = artist
        val retriever = MediaMetadataRetriever()
        val inputStream: InputStream?
        retriever.setDataSource(path)
        if (retriever.embeddedPicture != null) {
            inputStream = ByteArrayInputStream(retriever.embeddedPicture)
            Glide.with(holder.itemView.context).load(inputStream)
                    .into(holder.trackImage)
        }


    }

    override fun getItemCount(): Int {
        return tracklist.size
    }

    interface SongSelectedListener {
        fun onSongSelected(song: AudioModel, songs: List<AudioModel>)
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var trackImage: ImageView
        var title: TextView
        var artist: TextView

        init {
            trackImage = itemView.findViewById(R.id.imageView2)
            title = itemView.findViewById(R.id.textViewTitleTrack)
            artist = itemView.findViewById(R.id.textViewArtistTrack)
            itemView.setOnClickListener(this)

        }

        override fun onClick(view: View) {
            val song = tracklist[adapterPosition]
            mSongSelectedListener.onSongSelected(song, tracklist)

        }
    }
}
