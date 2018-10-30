package com.revosleap.bxplayer.AppUtils.RecyclerView.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.revosleap.bxplayer.AppUtils.Models.AudioModel;

import com.revosleap.bxplayer.AppUtils.Utils.AudioUtils;
import com.revosleap.bxplayer.AppUtils.Utils.ImageCover;
import com.revosleap.bxplayer.R;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.Holder> {
    List<AudioModel> tracklist;
    private final SongSelectedListener mSongSelectedListener;

    private Activity mActivity;

    public TrackAdapter(List<AudioModel> tracklist, final Activity activity,SongSelectedListener listener) {
        this.tracklist = tracklist;
        mActivity = activity;
       this.mSongSelectedListener = listener;

    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.track,parent,false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        AudioModel model= tracklist.get(position);
        String title= model.getTitle();
        String artist= model.getArtist();
        final String path= model.getPath();

        holder.artist.setText(title);
        holder.title.setText(artist);
//        if (AudioUtils.cover(path)!=null){
//            holder.trackImage.setImageBitmap(new ImageCover.execute(path));
//        }



    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return tracklist.size();
    }
    public interface SongSelectedListener {
        void onSongSelected(@NonNull final AudioModel song, @NonNull final List<AudioModel> songs);
    }

    public class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView trackImage;
        public TextView title,artist;
        public Holder(View itemView) {
            super(itemView);
            trackImage= itemView.findViewById(R.id.imageView2);
            title= itemView.findViewById(R.id.textViewTitleTrack);
            artist= itemView.findViewById(R.id.textViewArtistTrack);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            final AudioModel song = tracklist.get(getAdapterPosition());
            mSongSelectedListener.onSongSelected(song, tracklist);

        }
    }
}
