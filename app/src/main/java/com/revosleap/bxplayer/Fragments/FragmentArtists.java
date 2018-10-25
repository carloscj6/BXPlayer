package com.revosleap.bxplayer.Fragments;


import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.revosleap.bxplayer.AppUtils.Models.AudioModel;
import com.revosleap.bxplayer.AppUtils.Player.MusicNotificationManager;
import com.revosleap.bxplayer.AppUtils.Player.MusicService;
import com.revosleap.bxplayer.AppUtils.Player.PlaybackInfoListener;
import com.revosleap.bxplayer.AppUtils.Player.PlayerAdapter;
import com.revosleap.bxplayer.AppUtils.RecyclerView.Adapters.ArtistAdapter;
import com.revosleap.bxplayer.AppUtils.Utils.GetAudio;
import com.revosleap.bxplayer.R;

import java.util.List;


public class FragmentArtists extends Fragment {


    public FragmentArtists() {
        // Required empty public constructor
    }
    RecyclerView recyclerView;
    private MusicService mMusicService;
    private PlayerAdapter mPlayerAdapter;
    private MusicNotificationManager mMusicNotificationManager;
    private PlaybackListener mPlaybackListener;
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            mMusicService = ((MusicService.LocalBinder) iBinder).getInstance();
            mPlayerAdapter = mMusicService.getMediaPlayerHolder();
            mMusicNotificationManager = mMusicService.getMusicNotificationManager();


            if (mPlaybackListener == null) {
                mPlaybackListener = new PlaybackListener();
                mPlayerAdapter.setPlaybackInfoListener(mPlaybackListener);
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mMusicService = null;
        }
    };

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
    class PlaybackListener extends PlaybackInfoListener {

        @Override
        public void onPositionChanged(int position) {
//            if (!mUserIsSeeking) {
//                mSeekBarAudio.setProgress(position);
//            }
        }

        @Override
        public void onStateChanged(@State int state) {

            //updatePlayingStatus();
            if (mPlayerAdapter.getState() != State.RESUMED && mPlayerAdapter.getState() != State.PAUSED) {
             //   updatePlayingInfo(false, true);
            }
        }

//        @Override
//        public void onPlaybackCompleted() {
//
//            updateResetStatus
//                    (true);
//        }
    }
}
