package com.revosleap.bxplayer.ui.activities

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.app.LoaderManager
import android.support.v4.content.ContextCompat
import android.support.v4.content.Loader
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import com.revosleap.bxplayer.R
import com.revosleap.bxplayer.models.Artist
import com.revosleap.bxplayer.services.MusicPlayerService
import com.revosleap.bxplayer.ui.fragments.InfoFragment
import com.revosleap.bxplayer.utils.adapters.TabFragmentAdapter
import com.revosleap.bxplayer.utils.playback.PlayerAdapter
import com.revosleap.bxplayer.utils.utils.ArtistProvider
import com.revosleap.bxplayer.utils.utils.EqualizerUtils
import com.revosleap.bxplayer.utils.utils.Universal
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.android.synthetic.main.controls.*
import kotlinx.android.synthetic.main.tabs_main.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.toast
import org.jetbrains.anko.warn


class PlayerActivity : AppCompatActivity(), View.OnClickListener, AnkoLogger {

    private var mPlayerAdapter: PlayerAdapter? = null
    private var mSectionsPagerAdapter: TabFragmentAdapter? = null
    private var mMusicService: MusicPlayerService? = null
    private var isServiceBound = false
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mMusicService = (service as MusicPlayerService.MusicBinder).serviceInstance
            mPlayerAdapter = mMusicService!!.mediaPlayerHolder

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
        setViewPager()
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
        doBindService()

    }

    fun getPlayerService(): MusicPlayerService? {
        return if (mMusicService != null) {
            mMusicService!!
        } else null

    }

    private fun setViewPager() {
        tabsMain.apply {
            addTab(this.newTab().setText("Favorites"))
            addTab(this.newTab().setText("Playlists"))
            addTab(this.newTab().setText("Tracks"))
            addTab(this.newTab().setText("Albums"))
            addTab(this.newTab().setText("Artists"))
            this.tabGravity = TabLayout.GRAVITY_FILL
        }
        mSectionsPagerAdapter = TabFragmentAdapter(supportFragmentManager, 5)
        mViewPager.apply {
            adapter = mSectionsPagerAdapter
            currentItem = 2
            addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabsMain))
        }
        tabsMain.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(mViewPager))
    }

    private fun control() {
        constControls!!.setOnClickListener {
            getInfoFragment()
        }
    }
    private fun getInfoFragment() {
        val fragment = InfoFragment()
        val fragmentTag = fragment.tag
        val popped = supportFragmentManager.popBackStackImmediate(fragmentTag, 0)
        if (!popped && supportFragmentManager.findFragmentByTag(fragmentTag) == null) {
            supportFragmentManager
                    .beginTransaction()
                    .addToBackStack(fragmentTag)
                    .add(R.id.frame_current, fragment, fragmentTag)
                    .show(fragment)
                    .commit()

            constControls!!.visibility = View.GONE
            frame_music.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_player, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return if (id == R.id.action_settings) {
            openEqualizer()
            true
        } else super.onOptionsItemSelected(item)

    }


    override fun onBackPressed() {


        val fragments = supportFragmentManager.backStackEntryCount
        if (fragments == 1) {
            supportFragmentManager.popBackStackImmediate()
            frame_music.visibility = View.VISIBLE
            constControls!!.visibility = View.VISIBLE
        } else {
            frame_music.visibility = View.VISIBLE
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
            R.id.buttonPrev -> {
            }
            R.id.buttonPlay -> {
            }
            R.id.buttonNext -> {
            }
        }
    }

    private fun doBindService() {
        bindService(Intent(this@PlayerActivity,
                MusicPlayerService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
        isServiceBound = true
        val startNotStickyIntent = Intent(this@PlayerActivity, MusicPlayerService::class.java)
        startService(startNotStickyIntent)

    }

    private fun doUnbindService() {
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }

    private fun openEqualizer() {
        if (EqualizerUtils.hasEqualizer(this@PlayerActivity)) {
            if (checkIsPlayer()) {
                mPlayerAdapter?.openEqualizer(this@PlayerActivity)
            }
        } else {
            toast("Equalizer not Found!!")

        }
    }

    private fun checkIsPlayer(): Boolean {

        val isPlayer = mPlayerAdapter?.isMediaPlayer()
        if (!isPlayer!!) {
            EqualizerUtils.notifyNoSessionId(this@PlayerActivity)
        }
        return isPlayer
    }
}
