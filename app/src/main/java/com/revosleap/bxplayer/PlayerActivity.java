package com.revosleap.bxplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
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

import com.revosleap.bxplayer.AppUtils.Adapters.TabAdapter;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        ButterKnife.bind(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


//        title.setSelected(true);
//        artist.setSelected(true);
        checkPermissin();
        control();
        tabs();
        onViewClicked(findViewById(android.R.id.content));


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);




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
                break;
            case R.id.buttonPlay:
                break;
            case R.id.buttonNext:
                break;
        }
    }
}
