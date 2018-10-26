package com.revosleap.bxplayer.Fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.revosleap.bxplayer.AppUtils.Models.AudioModel;
import com.revosleap.bxplayer.AppUtils.RecyclerView.Adapters.TrackAdapter;
import com.revosleap.bxplayer.AppUtils.RecyclerView.Listeners.CustomTouchListener;
import com.revosleap.bxplayer.AppUtils.Player.AudioPlayerService;
import com.revosleap.bxplayer.AppUtils.Utils.GetAudio;
import com.revosleap.bxplayer.AppUtils.Utils.StorageUtil;
import com.revosleap.bxplayer.AppUtils.Utils.onItemClickListener;
import com.revosleap.bxplayer.R;

import java.util.ArrayList;
import java.util.List;


public class FragmentTracks extends Fragment  {

    RecyclerView recyclerView;
    private AudioPlayerService player;
    boolean serviceBound = false;
    List<AudioModel>list;
    public static final String Broadcast_PLAY_NEW_AUDIO = "com.revosleap.bxplayer.PlayNewAudio";

    private TrackAdapter adapter;





    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view= inflater.inflate(R.layout.fragment_tracks, container, false);


        list= new GetAudio().geAllAudio(getActivity());
        adapter= new TrackAdapter(list,getActivity());

        recyclerView = view.findViewById(R.id.trackRecycler);
        //TrackAdapter adapter= new TrackAdapter(list,getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addOnItemTouchListener(new CustomTouchListener(getActivity(), new onItemClickListener() {
            @Override
            public void onClick(View view, int index) {
               playAudio(index);
            }
        }));

        return view;


    }
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            AudioPlayerService.LocalBinder binder = (AudioPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("serviceStatus",serviceBound);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
//        serviceBound=savedInstanceState.getBoolean("serviceStatus");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void playAudio(int audioIndex) {
        ArrayList<AudioModel>arrayList = null;
        //Check is service is active
        if (!serviceBound) {
            //Store Serializable audioList to SharedPreferences
            StorageUtil storage = new StorageUtil(getActivity());
            storage.storeAudio(list);
            storage.storeAudioIndex(audioIndex);

            Intent playerIntent = new Intent(getActivity(), AudioPlayerService.class);
            getActivity().startService(playerIntent);
            getActivity().bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            //Store the new audioIndex to SharedPreferences
            StorageUtil storage = new StorageUtil(getActivity());
            storage.storeAudioIndex(audioIndex);

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            getActivity().sendBroadcast(broadcastIntent);
        }
    }



}
