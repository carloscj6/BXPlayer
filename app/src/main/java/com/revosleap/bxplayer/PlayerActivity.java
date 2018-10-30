package com.revosleap.bxplayer;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.revosleap.bxplayer.AppUtils.Adapters.TabAdapter;
import com.revosleap.bxplayer.AppUtils.BxPlayback.BXNotificationManager;
import com.revosleap.bxplayer.AppUtils.BxPlayback.BxPlayerService;
import com.revosleap.bxplayer.AppUtils.BxPlayback.PlaybackInfoListener;
import com.revosleap.bxplayer.AppUtils.BxPlayback.PlayerAdapter;
import com.revosleap.bxplayer.AppUtils.Models.AudioModel;
import com.revosleap.bxplayer.AppUtils.Utils.EqualizerUtils;
import com.revosleap.bxplayer.Fragments.InfoFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PlayerActivity extends AppCompatActivity {


    @BindView(R.id.Frame_music)
    FrameLayout frameMusic;


    ImageView cover;
    TextView title, artist;
    Button prev, next, play;
    @BindView(R.id.tabsMain)
    TabLayout tabLayout;
    @BindView(R.id.containerMain)
    ViewPager mViewPager;
    @BindView(R.id.imageView)
    ImageView imageView;
    @BindView(R.id.textViewArtName)
    TextView textViewArtName;
    @BindView(R.id.textViewTitle)
    TextView textViewTitle;
    @BindView(R.id.buttonPrev)
    Button buttonPrev;
    @BindView(R.id.buttonPlay)
    Button buttonPlay;
    @BindView(R.id.buttonNext)
    Button buttonNext;
    @BindView(R.id.constControls)
    ConstraintLayout layout;
    @BindView(R.id.Frame_current)
    FrameLayout FrameCurrent;
    private boolean mIsBound;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        ButterKnife.bind(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        checkPermissin();
        control();
        tabs();
        onViewClicked(findViewById(android.R.id.content));


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        doBindService();



    }

    @Override
    protected void onPause() {
        super.onPause();
        doUnbindService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        restorePlayerStatus();
        doBindService();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlaybackListener=null;
        doUnbindService();

    }

    private void tabs() {
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addTab(tabLayout.newTab().setText("Favorites"));
        tabLayout.addTab(tabLayout.newTab().setText("Playlist"));
        tabLayout.addTab(tabLayout.newTab().setText("Tracks"));
        tabLayout.addTab(tabLayout.newTab().setText("Albums"));
        tabLayout.addTab(tabLayout.newTab().setText("Artists"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        TabAdapter adapter = new TabAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        mViewPager.setAdapter(adapter);
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        mViewPager.setCurrentItem(2, true);
    }
    private void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }
    private void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(new Intent(this,
                BxPlayerService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;

        final Intent startNotStickyIntent = new Intent(this, BxPlayerService.class);
        startService(startNotStickyIntent);
        showStatus();
    }

    private void control() {
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InfoFragment fragment = new InfoFragment();
                String TAG = fragment.getTag();
                boolean popped = getSupportFragmentManager().popBackStackImmediate(TAG, 0);
                if (!popped && getSupportFragmentManager().findFragmentByTag(TAG) == null) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .addToBackStack(TAG)
                            .add(R.id.Frame_current, fragment, TAG)
                            .show(fragment)
                            .commit();

                    layout.setVisibility(View.GONE);
                    frameMusic.setVisibility(View.GONE);

                }


            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {


        int fragments = getSupportFragmentManager().getBackStackEntryCount();
        if (fragments == 1) {
            getSupportFragmentManager().popBackStackImmediate();
            frameMusic.setVisibility(View.VISIBLE);
            layout.setVisibility(View.VISIBLE);
        } else {
            frameMusic.setVisibility(View.VISIBLE);
            layout.setVisibility(View.VISIBLE);
            super.onBackPressed();

        }
    }

    private void checkPermissin() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    @OnClick({R.id.buttonPrev, R.id.buttonPlay, R.id.buttonNext})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.buttonPrev:
                skipNext();
                break;
            case R.id.buttonPlay:
                resumeOrPause();
                break;
            case R.id.buttonNext:
                skipPrev();
                break;
        }
    }
    private void updatePlayingStatus() {
        final int drawable = mPlayerAdapter.getState() != PlaybackInfoListener.State.PAUSED ?
                R.drawable.pause : R.drawable.play_icon;
        buttonPlay.post(new Runnable() {
            @Override
            public void run() {
                buttonPlay.setBackgroundResource(drawable);
            }
        });
    }
    private void showStatus(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mIsBound){
                    if (mPlayerAdapter!=null){
                        AudioModel selectedSong = mPlayerAdapter.getCurrentSong();

                        if (selectedSong!=null){
                            textViewTitle.setText(selectedSong.getTitle());
                            textViewArtName.setText(selectedSong.getArtist());
                        }

                    }

                }

            }
        },20);

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

        textViewTitle.post(new Runnable() {
            @Override
            public void run() {
                textViewTitle.setText(selectedSong.getTitle());
            }
        });

        textViewArtName.setText(selectedSong.getArtist());

        if (restore) {
            updatePlayingStatus();
            //updateResetStatus(false);

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
    private boolean checkIsPlayer() {

        boolean isPlayer = mPlayerAdapter.isMediaPlayer();
        if (!isPlayer) {
            EqualizerUtils.notifyNoSessionId(this);
        }
        return isPlayer;
    }
    public void reset() {
        if (checkIsPlayer()) {
            mPlayerAdapter.reset();
           // updateResetStatus(false);
        }
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
        if (EqualizerUtils.hasEqualizer(this)) {
            if (checkIsPlayer()) {
                mPlayerAdapter.openEqualizer(PlayerActivity.this);
            }
        } else {
            Toast.makeText(this, getString(R.string.no_eq), Toast.LENGTH_SHORT).show();
        }
    }
    private void restorePlayerStatus() {

       // mSeekBarAudio.setEnabled(mPlayerAdapter.isMediaPlayer());

        //if we are playing and the activity was restarted
        //update the controls panel
        if (mPlayerAdapter != null && mPlayerAdapter.isMediaPlayer()) {

            mPlayerAdapter.onResumeActivity();
            updatePlayingInfo(true, false);
        }
    }
    class PlaybackListener extends PlaybackInfoListener {

        @Override
        public void onPositionChanged(int position) {
//            if (!mUserIsSeeking) {
//                seekBarInfo.setProgress(position);
//            }
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
}
