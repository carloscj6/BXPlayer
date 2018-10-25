package com.revosleap.bxplayer;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.revosleap.bxplayer.AppUtils.Models.AudioModel;

import com.revosleap.bxplayer.AppUtils.Utils.AudioPlayerService;
import com.revosleap.bxplayer.Fragments.InfoFragment;
import com.revosleap.bxplayer.Fragments.MainFragment;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlayerActivity extends AppCompatActivity {


    public static final String Broadcast_PLAY_NEW_AUDIO = "com.revosleap.bxplayer.PlayNewAudio";
    @BindView(R.id.Frame_music)
    FrameLayout frameMusic;

    private AudioPlayerService player;
    boolean serviceBound = false;
    ArrayList<AudioModel> audioList;


    ImageView cover, mainbg;
    TextView title, artist;
    Button prev, next, play;
    ConstraintLayout layout;


    byte[] art;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        ButterKnife.bind(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        cover = findViewById(R.id.imageView);
        title = findViewById(R.id.textViewTitle);
        artist = findViewById(R.id.textViewArtName);
        prev = findViewById(R.id.buttonPrev);
        play = findViewById(R.id.buttonPlay);
        next = findViewById(R.id.buttonNext);
        layout = findViewById(R.id.constControls);

//        title.setSelected(true);
//        artist.setSelected(true);
        checkPermissin();
        control();
        loadMainFragment();


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cover2);








    }


    private void mediaInfo() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(PlayerActivity.this);
        String path = preferences.getString("CurrentPath", "");
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        try {
            art = retriever.getEmbeddedPicture();
            Bitmap image = BitmapFactory.decodeByteArray(art, 0, art.length);
            RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), image);
            cover.setImageDrawable(drawable);
            title.setText(retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_TITLE
            ));
            artist.setText(retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_ARTIST
            ));
        } catch (Exception e) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cover1);
            RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
            drawable.setCircular(true);
            cover.setImageDrawable(drawable);
            title.setText("Unknown Song Name");
            artist.setText("Unknown Artist");
        }
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
                            .hide(new MainFragment())
                            .show(fragment)

                            .commit();

                    layout.setVisibility(View.GONE);
                    frameMusic.setVisibility(View.GONE);

                }


            }
        });
    }

    private void loadMainFragment() {
        MainFragment fragment = new MainFragment();
        getSupportFragmentManager()
                .beginTransaction()

                .replace(R.id.Frame_music, fragment)
                .commit();

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
//            if (getFragmentManager().getBackStackEntryCount()>1){
//                getFragmentManager().popBackStack();
//            }
//            else {
//               super.onBackPressed();
//            }
        }
    }
    private void checkPermissin(){
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
                    !=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
            }
        }
    }
}
