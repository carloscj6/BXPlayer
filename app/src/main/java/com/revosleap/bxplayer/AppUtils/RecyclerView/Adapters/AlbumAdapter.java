package com.revosleap.bxplayer.AppUtils.RecyclerView.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.revosleap.bxplayer.AppUtils.Models.AudioModel;
import com.revosleap.bxplayer.AppUtils.RecyclerView.ViewHolder.AlbumViewHolder;
import com.revosleap.bxplayer.R;

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumViewHolder> {
    Context context;
    List<AudioModel> audioList;

    public AlbumAdapter(Context context, List<AudioModel> audioList) {
        this.context = context;
        this.audioList = audioList;
    }

    @Override
    public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.album,parent,false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AlbumViewHolder holder, int position) {
        AudioModel list= audioList.get(position);
        String artist= list.getArtist();
        String album= list.getAlbum();
        holder.albumArtist.setText(artist);
        holder.album.setText(album);
    }

    @Override
    public int getItemCount() {
        return audioList.size();
    }
}
