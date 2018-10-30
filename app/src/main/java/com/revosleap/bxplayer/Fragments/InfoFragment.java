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
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.revosleap.bxplayer.AppUtils.BxPlayback.BXNotificationManager;
import com.revosleap.bxplayer.AppUtils.BxPlayback.BxPlayerService;
import com.revosleap.bxplayer.AppUtils.BxPlayback.PlaybackInfoListener;
import com.revosleap.bxplayer.AppUtils.BxPlayback.PlayerAdapter;
import com.revosleap.bxplayer.AppUtils.Models.AudioModel;
import com.revosleap.bxplayer.AppUtils.Utils.AudioUtils;
import com.revosleap.bxplayer.AppUtils.Utils.EqualizerUtils;
import com.revosleap.bxplayer.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class InfoFragment extends Fragment {
    @BindView(R.id.imageViewInfo)
    ImageView imageViewInfo;
    @BindView(R.id.textViewInfoTitle)
    TextView textViewInfoTitle;
    @BindView(R.id.textViewInfoArtist)
    TextView textViewInfoArtist;
    @BindView(R.id.buttonInfoPlaylist)
    Button buttonInfoPlaylist;
    @BindView(R.id.buttonInfoFave)
    Button buttonInfoFave;
    @BindView(R.id.buttonInfoVol)
    Button buttonInfoVol;
    @BindView(R.id.constraintLayout2)
    ConstraintLayout constraintLayout2;
    @BindView(R.id.seekBarInfo)
    SeekBar seekBarInfo;
    @BindView(R.id.buttonInfoShuffle)
    Button buttonInfoShuffle;
    @BindView(R.id.buttonInfoPrev)
    Button buttonInfoPrev;
    @BindView(R.id.buttonInfoPlay)
    Button buttonInfoPlay;
    @BindView(R.id.buttonInfoNext)
    Button buttonInfoNext;
    @BindView(R.id.buttonInfoAll)
    Button buttonInfoAll;
    @BindView(R.id.constraintLayout3)
    ConstraintLayout constraintLayout3;
    Unbinder unbinder;
    boolean mUserIsSeeking,mIsBound;
    @BindView(R.id.textViewProgress)
    TextView textViewProgress;
    @BindView(R.id.textViewDuration)
    TextView textViewDuration;
    private String mSelectedArtist;
    private PlayerAdapter mPlayerAdapter;
    BxPlayerService mMusicService;
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


    public InfoFragment() {
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.info, container, false);
        unbinder = ButterKnife.bind(this, view);
        onViewClicked(view);
        doBindService();
        initializeSeekbar();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
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

    @OnClick({R.id.buttonInfoPlaylist, R.id.buttonInfoFave, R.id.buttonInfoVol, R.id.buttonInfoShuffle,
            R.id.buttonInfoPrev, R.id.buttonInfoPlay, R.id.buttonInfoNext, R.id.buttonInfoAll})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.buttonInfoPlaylist:
                break;
            case R.id.buttonInfoFave:
                break;
            case R.id.buttonInfoVol:
                break;
            case R.id.buttonInfoShuffle:
                break;
            case R.id.buttonInfoPrev:
                skipPrev();
                break;
            case R.id.buttonInfoPlay:
                resumeOrPause();
                break;
            case R.id.buttonInfoNext:
                skipNext();
                break;
            case R.id.buttonInfoAll:
                break;
        }
    }

    private void initializeSeekbar() {
        seekBarInfo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int userSelectedPosition = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    userSelectedPosition = progress;

                }
                textViewProgress.setText(AudioUtils.formatDuration(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mUserIsSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mUserIsSeeking) {
                  //  mSongPosition.setTextColor(currentPositionColor);
                }
                mUserIsSeeking = false;
                mPlayerAdapter.seekTo(userSelectedPosition);
            }
        });
    }
    private void updatePlayingStatus() {
        final int drawable = mPlayerAdapter.getState() != PlaybackInfoListener.State.PAUSED ?
                R.drawable.pause : R.drawable.play_icon;
       buttonInfoPlay.post(new Runnable() {
            @Override
            public void run() {
               buttonInfoPlay.setBackgroundResource(drawable);
            }
        });
    }
    private void updatePlayingInfo(boolean restore, boolean startPlay) {

        if (startPlay) {
            mPlayerAdapter.getMediaPlayer().start();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMusicService.startForeground(BXNotificationManager.NOTIFICATION_ID,
                            mMusicNotificationManager.createNotification());
                }
            }, 250);
        }

        final AudioModel selectedSong = mPlayerAdapter.getCurrentSong();

        mSelectedArtist = selectedSong.getArtist();
        final int duration = selectedSong.getDuration();
        seekBarInfo.setMax(duration);

        textViewDuration.setText(AudioUtils.formatDuration(duration));

        textViewInfoTitle.post(new Runnable() {
            @Override
            public void run() {
                textViewInfoTitle.setText(selectedSong.getTitle());
            }
        });

        textViewInfoArtist.setText(selectedSong.getArtist());

        if (restore) {
            seekBarInfo.setProgress(mPlayerAdapter.getPlayerPosition());
            updatePlayingStatus();
           // updateResetStatus(false);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //stop foreground if coming from pause state
                    if (mMusicService.isRestoredFromPause()) {
                        mMusicService.stopForeground(false);
                        mMusicService.getMusicNotificationManager().getNotificationManager()
                                .notify(BXNotificationManager.NOTIFICATION_ID,
                                        mMusicService.getMusicNotificationManager().getNotificationBuilder().build());
                        mMusicService.setRestoredFromPause(false);
                    }
                }
            }, 250);
        }
    }
    class PlaybackListener extends PlaybackInfoListener {

        @Override
        public void onPositionChanged(int position) {
            if (!mUserIsSeeking) {
                seekBarInfo.setProgress(position);
            }
        }

        @Override
        public void onStateChanged(@State int state) {

            updatePlayingStatus();
            if (mPlayerAdapter.getState() != State.RESUMED && mPlayerAdapter.getState() != State.PAUSED) {
                updatePlayingInfo(false, true);
            }
        }

        @Override
        public void onPlaybackCompleted() {
            //updateResetStatus(true);
        }
    }
    private boolean checkIsPlayer() {

        boolean isPlayer = mPlayerAdapter.isMediaPlayer();
        if (!isPlayer) {
            EqualizerUtils.notifyNoSessionId(getActivity());
        }
        return isPlayer;
    }
    public void skipPrev() {
        if (checkIsPlayer()) {
            mPlayerAdapter.instantReset();
        }
    }

    public void resumeOrPause() {
        if (checkIsPlayer()) {
            mPlayerAdapter.resumeOrPause();
        }
    }

    public void skipNext() {
        if (checkIsPlayer()) {
            mPlayerAdapter.skip(true);
        }
    }

    public void openEqualizer() {
        if (EqualizerUtils.hasEqualizer(getActivity())) {
            if (checkIsPlayer()) {
                mPlayerAdapter.openEqualizer(getActivity());
            }
        } else {
            Toast.makeText(getActivity(),"No equilizer found", Toast.LENGTH_SHORT).show();
        }
    }
}
