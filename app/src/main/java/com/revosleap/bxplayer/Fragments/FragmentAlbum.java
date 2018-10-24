package com.revosleap.bxplayer.Fragments;


import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.revosleap.bxplayer.AppUtils.Models.Album;
import com.revosleap.bxplayer.AppUtils.Models.Artist;
import com.revosleap.bxplayer.AppUtils.Models.AudioModel;
import com.revosleap.bxplayer.AppUtils.Models.Song;
import com.revosleap.bxplayer.AppUtils.Player.MusicNotificationManager;
import com.revosleap.bxplayer.AppUtils.Player.MusicService;
import com.revosleap.bxplayer.AppUtils.Player.PlaybackInfoListener;
import com.revosleap.bxplayer.AppUtils.Player.PlayerAdapter;
import com.revosleap.bxplayer.AppUtils.Player.Utils;
import com.revosleap.bxplayer.AppUtils.RecyclerView.Adapters.AlbumAdapter;
import com.revosleap.bxplayer.AppUtils.RecyclerView.Adapters.AlbumsAdapter;
import com.revosleap.bxplayer.AppUtils.RecyclerView.Adapters.ArtistAdapter;
import com.revosleap.bxplayer.AppUtils.RecyclerView.Adapters.ArtistsAdapter;
import com.revosleap.bxplayer.AppUtils.RecyclerView.Adapters.SongsAdapter;
import com.revosleap.bxplayer.AppUtils.Utils.ArtistProvider;
import com.revosleap.bxplayer.AppUtils.Utils.EqualizerUtils;
import com.revosleap.bxplayer.AppUtils.Utils.GetAudio;
import com.revosleap.bxplayer.AppUtils.Utils.RoundedFastScrollRecyclerView;
import com.revosleap.bxplayer.AppUtils.Utils.SongProvider;
import com.revosleap.bxplayer.R;

import java.util.Collections;
import java.util.List;


public class FragmentAlbum extends Fragment{
    private final int ANIMATION_DURATION = 500;
    private LinearLayoutManager mArtistsLayoutManager, mAlbumsLayoutManager, mSongsLayoutManager;
    private int mAccent;
    private boolean sThemeInverted;
    //private RoundedFastScrollRecyclerView mArtistsRecyclerView;
    private RecyclerView mAlbumsRecyclerView, mSongsRecyclerView,mArtistsRecyclerView;
    private AlbumsAdapter mAlbumsAdapter;
    private SongsAdapter mSongsAdapter;
    private TextView mPlayingAlbum, mPlayingSong, mDuration, mSongPosition, mArtistAlbumCount, mSelectedAlbum;
    private SeekBar mSeekBarAudio;
    private LinearLayout mControlsContainer;
    private View mSettingsView, mPlayerInfoView, mArtistDetails;
    private ImageView mPlayPauseButton, mResetButton, mExpandImage;
    private PlayerAdapter mPlayerAdapter;
    private boolean mUserIsSeeking = false;
    private List<Artist> mArtists;
    private String mSelectedArtist;
    private boolean sExpandArtistDiscography = false;
    private boolean sPlayerInfoLongPressed = false;
    private boolean sArtistDiscographyExpanded = false;
    private MusicService mMusicService;
    private PlaybackListener mPlaybackListener;
    private List<Song> mSelectedArtistSongs;
    private MusicNotificationManager mMusicNotificationManager;
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            mMusicService = ((MusicService.LocalBinder) iBinder).getInstance();
            mPlayerAdapter = mMusicService.getMediaPlayerHolder();
            mMusicNotificationManager = mMusicService.getMusicNotificationManager();
            //mMusicNotificationManager.setAccentColor(mAccent);

            if (mPlaybackListener == null) {
                mPlaybackListener = new PlaybackListener();
                mPlayerAdapter.setPlaybackInfoListener(mPlaybackListener);
            }
          //  checkReadStoragePermissions();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mMusicService = null;
        }
    };
    private boolean mIsBound;
    private Parcelable mSavedArtistRecyclerLayoutState;
    private Parcelable mSavedAlbumsRecyclerLayoutState;
    private Parcelable mSavedSongRecyclerLayoutState;


    public FragmentAlbum() {

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mArtistsLayoutManager != null && mAlbumsLayoutManager != null && mSongsLayoutManager != null) {
            mSavedArtistRecyclerLayoutState = mArtistsLayoutManager.onSaveInstanceState();
            mSavedAlbumsRecyclerLayoutState = mAlbumsLayoutManager.onSaveInstanceState();
            mSavedSongRecyclerLayoutState = mSongsLayoutManager.onSaveInstanceState();
        }
        if (mPlayerAdapter != null && mPlayerAdapter.isMediaPlayer()) {
            mPlayerAdapter.onPauseActivity();
        }
//        if (mSettingsView.getVisibility() == View.VISIBLE) {
//            revealView(mSettingsView, mControlsContainer, true, false);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_fragment_album, container, false);
        mAccent= getActivity().getResources().getColor(R.color.colorAccentLight);
        getViews(view);
        //initializeSettings();

       // setupViewParams();

       //initializeSeekBar();

        doBindService();
        return view;
    }
    private void getViews(View view){
        mArtistsRecyclerView = view.findViewById(R.id.artists_rv);
        mControlsContainer = view.findViewById(R.id.controls_container);

        mArtistDetails = view.findViewById(R.id.artist_details);
        mPlayerInfoView = view.findViewById(R.id.player_info);
        mPlayingSong = view.findViewById(R.id.playing_song);
        mPlayingAlbum = view.findViewById(R.id.playing_album);
        mExpandImage = view.findViewById(R.id.expand);
        setupPlayerInfoTouchBehaviour();

        mPlayPauseButton = view.findViewById(R.id.play_pause);
//
        mResetButton = view.findViewById(R.id.replay);
        mSeekBarAudio = view.findViewById(R.id.seekTo);

        mDuration = view.findViewById(R.id.duration);
        mSongPosition = view.findViewById(R.id.song_position);
        mArtistAlbumCount = view.findViewById(R.id.artist_album_count);
        mSelectedAlbum = view.findViewById(R.id.selected_disc);

        mArtistsRecyclerView = view.findViewById(R.id.artists_rv);

      //  mArtistsRecyclerView.setTrackColor(ColorUtils.setAlphaComponent(ContextCompat.getColor(getActivity(), mAccent), sThemeInverted ? 15 : 30));

        mAlbumsRecyclerView = view.findViewById(R.id.albums_rv);
        mSongsRecyclerView = view.findViewById(R.id.songs_rv);

       // mSettingsView = findViewById(R.id.settings_view);
    }
    private void setupPlayerInfoTouchBehaviour() {
        mPlayerInfoView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!sPlayerInfoLongPressed) {
                    mPlayingSong.setSelected(true);
                    mPlayingAlbum.setSelected(true);
                    sPlayerInfoLongPressed = true;
                }
                return true;
            }
        });
        mPlayerInfoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (sPlayerInfoLongPressed) {
                        mPlayingSong.setSelected(false);
                        mPlayingAlbum.setSelected(false);
                        sPlayerInfoLongPressed = false;
                    }
                }
                return false;
            }
        });
    }
    private void setupViewParams() {
        final ViewTreeObserver observer = mControlsContainer.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int controlsContainerHeight = mControlsContainer.getHeight();

                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mArtistsRecyclerView.getLayoutParams();
                layoutParams.topMargin = mPlayerInfoView.getHeight();
                mArtistsRecyclerView.setLayoutParams(layoutParams);
                mSettingsView.setMinimumHeight(controlsContainerHeight);
                mControlsContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void setArtistsRecyclerView(@NonNull final List<Artist> data) {
        mArtistsLayoutManager = new LinearLayoutManager(getActivity());
        mArtistsRecyclerView.setLayoutManager(mArtistsLayoutManager);
        final ArtistsAdapter artistsAdapter = new ArtistsAdapter(getActivity(), data);
        mArtistsRecyclerView.setAdapter(artistsAdapter);
    }
    private void updatePlayingInfo(boolean restore, boolean startPlay) {

        if (startPlay) {
            mPlayerAdapter.getMediaPlayer().start();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMusicService.startForeground(MusicNotificationManager.NOTIFICATION_ID, mMusicNotificationManager.createNotification());
                }
            }, 250);
        }

        final Song selectedSong = mPlayerAdapter.getCurrentSong();

        mSelectedArtist = selectedSong.artistName;
        final int duration = selectedSong.duration;
        mSeekBarAudio.setMax(duration);
        Utils.updateTextView(mDuration, Song.formatDuration(duration));

        final Spanned spanned = Utils.buildSpanned(getString(R.string.playing_song, mSelectedArtist, selectedSong.title));

        mPlayingSong.post(new Runnable() {
            @Override
            public void run() {
                mPlayingSong.setText(spanned);
            }
        });

        Utils.updateTextView(mPlayingAlbum, selectedSong.albumName);

        if (restore) {
            mSeekBarAudio.setProgress(mPlayerAdapter.getPlayerPosition());
            //updatePlayingStatus();
           // updateResetStatus(false);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //stop foreground if coming from pause state
                    if (mMusicService.isRestoredFromPause()) {
                        mMusicService.stopForeground(false);
                        mMusicService.getMusicNotificationManager().getNotificationManager().notify(MusicNotificationManager.NOTIFICATION_ID, mMusicService.getMusicNotificationManager().getNotificationBuilder().build());
                        mMusicService.setRestoredFromPause(false);
                    }
                }
            }, 250);
        }
    }
    private void restorePlayerStatus() {

        mSeekBarAudio.setEnabled(mPlayerAdapter.isMediaPlayer());

        //if we are playing and the activity was restarted
        //update the controls panel
        if (mPlayerAdapter != null && mPlayerAdapter.isMediaPlayer()) {

            mPlayerAdapter.onResumeActivity();
            updatePlayingInfo(true, false);
        }
    }

    private void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        getActivity().bindService(new Intent(getActivity(),
                MusicService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;

        final Intent startNotStickyIntent = new Intent(getActivity(), MusicService.class);
        getActivity().startService(startNotStickyIntent);
    }

    private void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
           getActivity().unbindService(mConnection);
            mIsBound = false;
        }
    }

    private void setArtistDetails(List<Album> albums) {
        if (mAlbumsAdapter != null) {
            mAlbumsRecyclerView.scrollToPosition(0);
            //only notify recycler view of item changed if an adapter already exists
            mAlbumsAdapter.swapArtist(albums);
        } else {
            mAlbumsLayoutManager = new LinearLayoutManager(getActivity(),
                    LinearLayoutManager.HORIZONTAL, false);
            mAlbumsRecyclerView.setLayoutManager(mAlbumsLayoutManager);
            mAlbumsAdapter = new AlbumsAdapter(getActivity(), albums,
                    mPlayerAdapter, ContextCompat.getColor(getActivity(), mAccent));
            mAlbumsRecyclerView.setAdapter(mAlbumsAdapter);
        }

        mSelectedArtistSongs = SongProvider.getAllArtistSongs(albums);
        Utils.updateTextView(mArtistAlbumCount, getString(R.string.albums, mSelectedArtist, albums.size()));

        if (sExpandArtistDiscography) {
            revealView(mArtistDetails, mArtistsRecyclerView, false, true);
            sExpandArtistDiscography = false;
        } else {
            restorePlayerStatus();
        }
    }





    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPlaybackListener = null;
        doUnbindService();
    }


    public void expandArtistDetails(View v) {
        revealView(mArtistDetails, mArtistsRecyclerView, false, !sArtistDiscographyExpanded);
    }

    private void rotateExpandImage(final boolean expand) {

        final int ALPHA_DURATION = 250;
        final float PIVOT_VALUE = 0.5f;
        final int PIVOT_TYPE = RotateAnimation.RELATIVE_TO_SELF;
        final float from = expand ? 0.0f : 180.0f;
        final float to = expand ? 180.0f : 0.0f;

        final AnimationSet animSet = new AnimationSet(true);
        animSet.setInterpolator(new DecelerateInterpolator());
        animSet.setFillAfter(true);
        animSet.setFillEnabled(true);
        final RotateAnimation animRotate = new RotateAnimation(from, to, PIVOT_TYPE, PIVOT_VALUE, PIVOT_TYPE, PIVOT_VALUE);
        animRotate.setDuration(ANIMATION_DURATION);
        animRotate.setFillAfter(true);
        animRotate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (!expand) {
                    mExpandImage.setVisibility(View.VISIBLE);
                    mExpandImage.animate().alpha(1.0f).setDuration(ALPHA_DURATION).start();
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (expand) {
                    mExpandImage.animate().alpha(0.0f).setDuration(ALPHA_DURATION).start();
                    mExpandImage.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        animSet.addAnimation(animRotate);
        mExpandImage.startAnimation(animSet);
    }

    private void revealView(final View viewToReveal, final View viewToHide, final boolean isSettings, boolean show) {

        final int viewToRevealHeight = viewToReveal.getHeight();
        final int viewToRevealWidth = viewToReveal.getWidth();
        final int viewToRevealHalfWidth = viewToRevealWidth / 2;
        final int radius = (int) Math.hypot(viewToRevealWidth, viewToRevealHeight);
        final int fromY = isSettings ? viewToRevealHeight / 2 : viewToHide.getTop() / 2;

        if (show) {
            final Animator anim = ViewAnimationUtils.createCircularReveal(viewToReveal, viewToRevealHalfWidth, fromY, 0, radius);
            anim.setDuration(ANIMATION_DURATION);
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    if (!isSettings) {
                        rotateExpandImage(true);
                    }
                    viewToReveal.setVisibility(View.VISIBLE);
                    viewToHide.setVisibility(View.INVISIBLE);
                    viewToReveal.setClickable(false);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    if (!isSettings) {
                        sArtistDiscographyExpanded = true;
                    }
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            });
            anim.start();

        } else {

            final Animator anim = ViewAnimationUtils.createCircularReveal(viewToReveal, viewToRevealHalfWidth, fromY, radius, 0);
            anim.setDuration(ANIMATION_DURATION);
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    if (!isSettings) {
                        rotateExpandImage(false);
                    }
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    viewToReveal.setVisibility(View.INVISIBLE);
                    viewToHide.setVisibility(View.VISIBLE);
                    viewToReveal.setClickable(true);
                    if (!isSettings) {
                        sArtistDiscographyExpanded = false;
                    }
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            });
            anim.start();
        }
    }
    private void initializeSeekBar() {
        mSeekBarAudio.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                  //  final int currentPositionColor = mSongPosition.getCurrentTextColor();
                    final int currentPositionColor= getActivity().getResources().getColor(R.color.colorAccentLight);
                    int userSelectedPosition = 0;

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        mUserIsSeeking = true;
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                        if (fromUser) {
                            userSelectedPosition = progress;
                            mSongPosition.setTextColor(ContextCompat.getColor(getActivity(), mAccent));
                        }
                        mSongPosition.setText(Song.formatDuration(progress));
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                        if (mUserIsSeeking) {
                            mSongPosition.setTextColor(currentPositionColor);
                        }
                        mUserIsSeeking = false;
                        mPlayerAdapter.seekTo(userSelectedPosition);
                    }
                });
    }
    private void updateResetStatus(boolean onPlaybackCompletion) {
        final int color = onPlaybackCompletion ? Color.BLACK : mPlayerAdapter.isReset() ? Color.WHITE : Color.BLACK;
        mResetButton.post(new Runnable() {
            @Override
            public void run() {
                mResetButton.getDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
            }
        });
    }

    public void reset(View v) {
        if (checkIsPlayer()) {
            mPlayerAdapter.reset();
            updateResetStatus(false);
        }
    }

    public void skipPrev(View v) {
        if (checkIsPlayer()) {
            mPlayerAdapter.instantReset();
        }
    }

    public void resumeOrPause(View v) {
        if (checkIsPlayer()) {
            mPlayerAdapter.resumeOrPause();
        }
    }

    public void skipNext(View v) {
        if (checkIsPlayer()) {
            mPlayerAdapter.skip(true);
        }
    }

    public void openEqualizer(View v) {
        if (EqualizerUtils.hasEqualizer(getActivity())) {
            if (checkIsPlayer()) {
                mPlayerAdapter.openEqualizer(getActivity());
            }
        } else {
            Toast.makeText(getActivity(), getString(R.string.no_eq), Toast.LENGTH_SHORT).show();
        }
    }

    public void openGitPage(View v) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/enricocid/Music-Player-GO")));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getActivity(), getString(R.string.no_browser), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private boolean checkIsPlayer() {

        boolean isPlayer = mPlayerAdapter.isMediaPlayer();
        if (!isPlayer) {
            EqualizerUtils.notifyNoSessionId(getActivity());
        }
        return isPlayer;
    }

    class PlaybackListener extends PlaybackInfoListener {

        @Override
        public void onPositionChanged(int position) {
            if (!mUserIsSeeking) {
                mSeekBarAudio.setProgress(position);
            }
        }

        @Override
        public void onStateChanged(@State int state) {

            //updatePlayingStatus();
            if (mPlayerAdapter.getState() != State.RESUMED && mPlayerAdapter.getState() != State.PAUSED) {
                updatePlayingInfo(false, true);
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
