package com.revosleap.bxplayer.ui.activities

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.revosleap.bxplayer.R
import com.revosleap.bxplayer.ui.fragments.InfoFragment
import com.revosleap.bxplayer.utils.adapters.TabAdapter
import com.revosleap.bxplayer.utils.playback.BXNotificationManager
import com.revosleap.bxplayer.utils.playback.BxPlayerService
import com.revosleap.bxplayer.utils.playback.PlaybackInfoListener
import com.revosleap.bxplayer.utils.playback.PlayerAdapter
import com.revosleap.bxplayer.utils.utils.EqualizerUtils
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.android.synthetic.main.controls.*
import kotlinx.android.synthetic.main.tabs_main.*

class PlayerActivity : AppCompatActivity(), View.OnClickListener {

    private var mIsBound: Boolean = false
    private var mPlayerAdapter: PlayerAdapter? = null
    internal var mMusicService: BxPlayerService? = null
    private var mMusicNotificationManager: BXNotificationManager? = null
    private var mPlaybackListener: PlaybackListener? = null
    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mMusicService = (service as BxPlayerService.BXBinder).instance
            mPlayerAdapter = mMusicService!!.mediaPlayerHolder
            mMusicNotificationManager = mMusicService!!.musicNotificationManager
            if (mPlaybackListener == null) {
                mPlaybackListener = PlaybackListener()
                mPlayerAdapter!!.setPlaybackInfoListener(mPlaybackListener!!)
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mMusicService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        doBindService()
        checkPermission()
        control()
        tabs()
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        buttonNext.setOnClickListener(this)
        buttonPlay.setOnClickListener(this)
        buttonPrev.setOnClickListener(this)


    }

    override fun onPause() {
        super.onPause()
        doUnbindService()
    }

    override fun onResume() {
        super.onResume()
        restorePlayerStatus()
        doBindService()

    }

    override fun onDestroy() {
        super.onDestroy()
        mPlaybackListener = null
        doUnbindService()

    }

    private fun tabs() {
        mViewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabsMain))
        tabsMain!!.addTab(tabsMain!!.newTab().setText("Favorites"))
        tabsMain.apply {
            addTab(this.newTab().setText("Favorites"))
            addTab(this.newTab().setText("Playlists"))
            addTab(this.newTab().setText("Tracks"))
            addTab(this.newTab().setText("Albums"))
            addTab(this.newTab().setText("Artists"))
            this.tabGravity = TabLayout.GRAVITY_FILL
        }
        val adapter = TabAdapter(supportFragmentManager, tabsMain.tabCount)
        mViewPager!!.adapter = adapter
        tabsMain.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(mViewPager))
        mViewPager!!.setCurrentItem(2, true)
    }

    private fun doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection)
            mIsBound = false
        }
    }

    private fun doBindService() {
        bindService(Intent(this,
                BxPlayerService::class.java), mConnection, Context.BIND_AUTO_CREATE)
        mIsBound = true

        val startNotStickyIntent = Intent(this, BxPlayerService::class.java)
        startService(startNotStickyIntent)
        showStatus()
    }

    private fun control() {
        constControls!!.setOnClickListener {
            val fragment = InfoFragment()
            val TAG = fragment.tag
            val popped = supportFragmentManager.popBackStackImmediate(TAG, 0)
            if (!popped && supportFragmentManager.findFragmentByTag(TAG) == null) {
                supportFragmentManager
                        .beginTransaction()
                        .addToBackStack(TAG)
                        .add(R.id.frame_current, fragment, TAG)
                        .show(fragment)
                        .commit()

                constControls!!.visibility = View.GONE
                frame_music.setVisibility(View.GONE)

            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_player, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)

    }


    override fun onBackPressed() {


        val fragments = supportFragmentManager.backStackEntryCount
        if (fragments == 1) {
            supportFragmentManager.popBackStackImmediate()
            frame_music.setVisibility(View.VISIBLE)
            constControls!!.visibility = View.VISIBLE
        } else {
            frame_music.setVisibility(View.VISIBLE)
            constControls!!.visibility = View.VISIBLE
            super.onBackPressed()

        }
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.buttonPrev -> skipNext()
            R.id.buttonPlay -> resumeOrPause()
            R.id.buttonNext -> skipPrev()
        }
    }


    private fun updatePlayingStatus() {
        val drawable = if (mPlayerAdapter!!.state != PlaybackInfoListener.State.PAUSED)
            R.drawable.pause
        else
            R.drawable.play_icon
        buttonPlay!!.post { buttonPlay!!.setBackgroundResource(drawable) }
    }

    private fun showStatus() {
        Handler().postDelayed({
            if (mIsBound) {
                if (mPlayerAdapter != null) {
                    val selectedSong = mPlayerAdapter!!.currentSong

                    if (selectedSong != null) {
                        textViewTitle!!.text = selectedSong.title
                        textViewArtName!!.text = selectedSong.artist
                    }

                }

            }
        }, 20)

    }

    private fun updatePlayingInfo(restore: Boolean, startPlay: Boolean) {

        if (startPlay) {
            mPlayerAdapter!!.mediaPlayer.start()
            Handler().postDelayed({
                mMusicService!!.startForeground(BXNotificationManager.NOTIFICATION_ID,
                        mMusicNotificationManager!!.createNotification())
            }, 250)
        }

        val selectedSong = mPlayerAdapter!!.currentSong

        textViewTitle!!.post { textViewTitle!!.text = selectedSong.title }

        textViewArtName!!.text = selectedSong.artist

        if (restore) {
            updatePlayingStatus()
            //updateResetStatus(false);

            Handler().postDelayed({
                //stop foreground if coming from pause state
                if (mMusicService!!.isRestoredFromPause) {
                    mMusicService!!.stopForeground(false)
                    mMusicService!!.musicNotificationManager?.notificationManager!!
                            .notify(BXNotificationManager.NOTIFICATION_ID,
                                    mMusicService!!.musicNotificationManager?.notificationBuilder?.build())
                    mMusicService!!.isRestoredFromPause = false
                }
            }, 250)
        }
    }

    private fun checkIsPlayer(): Boolean {

        val isPlayer = mPlayerAdapter!!.isMediaPlayer
        if (!isPlayer) {
            EqualizerUtils.notifyNoSessionId(this)
        }
        return isPlayer
    }

    fun reset() {
        if (checkIsPlayer()) {
            mPlayerAdapter!!.reset()
            // updateResetStatus(false);
        }
    }

    fun skipPrev() {
        if (checkIsPlayer()) {
            mPlayerAdapter!!.instantReset()
        }
    }

    fun resumeOrPause() {
        if (checkIsPlayer()) {
            mPlayerAdapter!!.resumeOrPause()
        }
    }

    fun skipNext() {
        if (checkIsPlayer()) {
            mPlayerAdapter!!.skip(true)
        }
    }

    fun openEqualizer() {
        if (EqualizerUtils.hasEqualizer(this)) {
            if (checkIsPlayer()) {
                mPlayerAdapter!!.openEqualizer(this@PlayerActivity)
            }
        } else {
            Toast.makeText(this, getString(R.string.no_eq), Toast.LENGTH_SHORT).show()
        }
    }

    private fun restorePlayerStatus() {

        // mSeekBarAudio.setEnabled(mPlayerAdapter.isMediaPlayer());

        //if we are playing and the activity was restarted
        //update the controls panel
        if (mPlayerAdapter != null && mPlayerAdapter!!.isMediaPlayer) {

            mPlayerAdapter!!.onResumeActivity()
            updatePlayingInfo(true, false)
        }
    }

    internal inner class PlaybackListener : PlaybackInfoListener() {

        override fun onPositionChanged(position: Int) {
            //            if (!mUserIsSeeking) {
            //                seekBarInfo.setProgress(position);
            //            }
        }

        override fun onStateChanged(@State state: Int) {

            updatePlayingStatus()
            if (mPlayerAdapter!!.state != PlaybackInfoListener.State.RESUMED && mPlayerAdapter!!.state != PlaybackInfoListener.State.PAUSED) {
                updatePlayingInfo(false, true)
            }
        }

        override fun onPlaybackCompleted() {
            //updateResetStatus(true);
        }
    }
}
