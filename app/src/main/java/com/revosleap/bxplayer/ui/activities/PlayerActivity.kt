package com.revosleap.bxplayer.ui.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import com.revosleap.bxplayer.R
import com.revosleap.bxplayer.ui.fragments.InfoFragment
import com.revosleap.bxplayer.utils.adapters.TabFragmentAdapter
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.android.synthetic.main.controls.*
import kotlinx.android.synthetic.main.tabs_main.*


class PlayerActivity : AppCompatActivity(), View.OnClickListener {
    private var mSectionsPagerAdapter: TabFragmentAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)

        checkPermission()
        control()
        setViewPager()
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        buttonNext.setOnClickListener(this)
        buttonPlay.setOnClickListener(this)
        buttonPrev.setOnClickListener(this)


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


}
