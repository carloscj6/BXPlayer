package com.revosleap.bxplayer.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.revosleap.bxplayer.R;


public class FragmentAlbum extends Fragment{



    public FragmentAlbum() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_fragment_album, container, false);


        return view;
    }



}
