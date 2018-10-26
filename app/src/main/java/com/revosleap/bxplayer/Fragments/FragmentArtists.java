package com.revosleap.bxplayer.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.revosleap.bxplayer.AppUtils.Models.AudioModel;

import com.revosleap.bxplayer.AppUtils.RecyclerView.Adapters.ArtistAdapter;
import com.revosleap.bxplayer.AppUtils.Utils.GetAudio;
import com.revosleap.bxplayer.R;

import java.util.List;


public class FragmentArtists extends Fragment {


    public FragmentArtists() {
        // Required empty public constructor
    }
    RecyclerView recyclerView;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.fragment_artists, container, false);
        recyclerView= view.findViewById(R.id.artistrecycler);
        List<AudioModel>models= new GetAudio().geAllAudio(getActivity());
        ArtistAdapter adapter= new ArtistAdapter(models,getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);


    }

}
