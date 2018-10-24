package com.revosleap.bxplayer.AppUtils.RecyclerView.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.revosleap.bxplayer.R;

public class TrackViewHolder extends RecyclerView.ViewHolder {
   public ImageView trackImage;
   public TextView title,artist;
    public TrackViewHolder(View itemView) {
        super(itemView);
        trackImage= itemView.findViewById(R.id.imageView2);
        title= itemView.findViewById(R.id.textViewTitleTrack);
        artist= itemView.findViewById(R.id.textViewArtistTrack);

    }
}
