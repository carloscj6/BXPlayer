package com.revosleap.bxplayer.AppUtils.RecyclerView.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.revosleap.bxplayer.R;

public class AlbumViewHolder extends RecyclerView.ViewHolder {
    public ImageView albumCover;
    public TextView album, albumArtist;

    public AlbumViewHolder(View itemView) {
        super(itemView);
        albumCover= itemView.findViewById(R.id.imageViewAlbum);
        album= itemView.findViewById(R.id.textViewAlbumName);
        albumArtist= itemView.findViewById(R.id.textViewAlbumArtist);
    }
}
