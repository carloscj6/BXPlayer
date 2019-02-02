package com.revosleap.bxplayer.ui.activities

import android.support.design.widget.TabLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import com.revosleap.bxplayer.R
import com.revosleap.bxplayer.utils.adapters.TabFragmentAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.view.*

class MainActivity : AppCompatActivity() {

    private var mSectionsPagerAdapter: TabFragmentAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
       setViewPager()


    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }


    private fun setViewPager(){
        tabs.apply {
            addTab(this.newTab().setText("Favorites"))
            addTab(this.newTab().setText("Playlists"))
            addTab(this.newTab().setText("Tracks"))
            addTab(this.newTab().setText("Albums"))
            addTab(this.newTab().setText("Artists"))
            this.tabGravity = TabLayout.GRAVITY_FILL
        }
        mSectionsPagerAdapter = TabFragmentAdapter(supportFragmentManager,5)
        container.adapter = mSectionsPagerAdapter
        container.currentItem = 2
        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))
    }

}
