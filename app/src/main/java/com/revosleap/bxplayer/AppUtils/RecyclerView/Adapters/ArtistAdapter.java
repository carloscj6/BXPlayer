package com.revosleap.bxplayer.AppUtils.RecyclerView.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.revosleap.bxplayer.AppUtils.Models.AudioModel;
import com.revosleap.bxplayer.AppUtils.RecyclerView.ViewHolder.TrackViewHolder;
import com.revosleap.bxplayer.R;

import java.util.List;

public class ArtistAdapter extends RecyclerView.Adapter<TrackViewHolder> {
    List<AudioModel>artistList;
    Context context;

    public ArtistAdapter(List<AudioModel> artistList, Context context) {
        this.artistList = artistList;
        this.context = context;
    }

    @Override
    public TrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.track,parent,false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrackViewHolder holder, int position) {
        AudioModel list= artistList.get(position);
        String artist= list.getArtist();
        String track= list.getTitle();

        holder.title.setText(artist);
        holder.artist.setText(track);
    }

    @Override
    public int getItemCount() {
        return artistList.size();
    }
}
