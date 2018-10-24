package com.revosleap.bxplayer.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.revosleap.bxplayer.R;


public class InfoFragment extends Fragment {
    public InfoFragment() {
    }

    ImageView art;
    TextView title,artist;
    Button playlist,favorite,volume,shuffle,prev,play,next,all;
    SeekBar seekBar;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view= inflater.inflate (R.layout.info, container, false);
        inst(view);
        return view;
    }
    private void inst(View view){
        art= view.findViewById(R.id.imageViewInfo);
        title= view.findViewById(R.id.textViewInfoTitle);
        artist= view.findViewById(R.id.textViewInfoArtist);
        playlist= view.findViewById(R.id.buttonInfoPlaylist);
        favorite= view.findViewById(R.id.buttonInfoFave);
        volume= view.findViewById(R.id.buttonInfoVol);
        shuffle= view.findViewById(R.id.buttonInfoShuffle);
        prev= view.findViewById(R.id.buttonInfoPrev);
        play= view.findViewById(R.id.buttonInfoPlay);
        next= view.findViewById(R.id.buttonInfoNext);
        all= view.findViewById(R.id.buttonInfoAll);
        seekBar= view.findViewById(R.id.seekBarInfo);
    }
}
