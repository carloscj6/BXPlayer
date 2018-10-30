package com.revosleap.bxplayer.Fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.revosleap.bxplayer.AppUtils.BxPlayback.BXNotificationManager;
import com.revosleap.bxplayer.AppUtils.BxPlayback.BxPlayerService;
import com.revosleap.bxplayer.AppUtils.BxPlayback.PlaybackInfoListener;
import com.revosleap.bxplayer.AppUtils.BxPlayback.PlayerAdapter;
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


public class FragmentTracks extends Fragment implements TrackAdapter.SongSelectedListener {

    RecyclerView recyclerView;
    private AudioPlayerService player;
    boolean serviceBound = false;
    List<AudioModel>list;
    public static final String Broadcast_PLAY_NEW_AUDIO = "com.revosleap.bxplayer.PlayNewAudio";
    BxPlayerService mMusicService;
    private boolean mIsBound;
    private TrackAdapter adapter;
    private PlayerAdapter mPlayerAdapter;
    private BXNotificationManager mMusicNotificationManager;
    private PlaybackListener mPlaybackListener;
    private ServiceConnection mConnection= new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMusicService=((BxPlayerService.BXBinder)service).getInstance();
            mPlayerAdapter = mMusicService.getMediaPlayerHolder();
            mMusicNotificationManager = mMusicService.getMusicNotificationManager();
            if (mPlaybackListener == null) {
                mPlaybackListener = new PlaybackListener();
                mPlayerAdapter.setPlaybackInfoListener(mPlaybackListener);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMusicService = null;
        }
    };





    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view= inflater.inflate(R.layout.fragment_tracks, container, false);


        list= new GetAudio().geAllAudio(getActivity());
        adapter= new TrackAdapter(list,getActivity(),this);

        recyclerView = view.findViewById(R.id.trackRecycler);
        //TrackAdapter adapter= new TrackAdapter(list,getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        recyclerView.addOnItemTouchListener(new CustomTouchListener(getActivity(), new onItemClickListener() {
//            @Override
//            public void onClick(View view, int index) {
//               playAudio(index);
//            }
//        }));

        return view;


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        doBindService();
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
        super.onDestroy();
        mPlaybackListener = null;
        doUnbindService();
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


    @Override
    public void onSongSelected(@NonNull AudioModel song, @NonNull List<AudioModel> songs) {
        Log.v("song ",song.getTitle()+" number= "+songs.size());
        try {
            mPlayerAdapter.setCurrentSong(song, songs);
            mPlayerAdapter.initMediaPlayer();
        }catch (Exception e){
            e.printStackTrace();
        }
        mPlayerAdapter.getMediaPlayer().start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mMusicService.startForeground(BXNotificationManager.NOTIFICATION_ID,
                        mMusicNotificationManager.createNotification());
            }
        }, 250);

    }
    private void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        getActivity().bindService(new Intent(getActivity(),
                BxPlayerService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;

        final Intent startNotStickyIntent = new Intent(getActivity(),BxPlayerService.class);
        getActivity().startService(startNotStickyIntent);
    }
    private void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            getActivity().unbindService(mConnection);
            mIsBound = false;
        }
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
//
//            updatePlayingStatus();
//            if (mPlayerAdapter.getState() != State.RESUMED && mPlayerAdapter.getState() != State.PAUSED) {
//                updatePlayingInfo(false, true);
//            }
        }

        @Override
        public void onPlaybackCompleted() {
            //updateResetStatus(true);
        }
    }
}
