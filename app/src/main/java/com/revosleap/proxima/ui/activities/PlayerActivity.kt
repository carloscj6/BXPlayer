package com.revosleap.proxima.ui.activities

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.graphics.Palette
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.revosleap.proxima.R
import com.revosleap.proxima.callbacks.BXColor
import com.revosleap.proxima.models.Song
import com.revosleap.proxima.services.MusicPlayerService
import com.revosleap.proxima.ui.fragments.FragmentInfo
import com.revosleap.proxima.utils.adapters.MainTabsAdapter
import com.revosleap.proxima.utils.playback.BXNotificationManager
import com.revosleap.proxima.utils.playback.PlaybackInfoListener
import com.revosleap.proxima.callbacks.PlayerAdapter
import com.revosleap.proxima.utils.utils.EqualizerUtils
import com.revosleap.proxima.utils.utils.PreferenceHelper
import com.revosleap.proxima.utils.utils.Universal
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.android.synthetic.main.controls.*
import kotlinx.android.synthetic.main.tabs_main.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.startService
import org.jetbrains.anko.toast
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.lang.reflect.Type


class PlayerActivity : AppCompatActivity(), View.OnClickListener, AnkoLogger {
    private var preferenceHelper: PreferenceHelper? = null
    private var mPlayerAdapter: PlayerAdapter? = null
    private var mSectionsPagerAdapter: MainTabsAdapter? = null
    private var mMusicService: MusicPlayerService? = null
    private var mPlaybackListener: PlaybackListener? = null
    private var mMusicNotificationManager: BXNotificationManager? = null
    private var currentSongs = mutableListOf<Song>()
    private var currentPosition = 0
    private var isPlayingNew = false
    private var isServiceBound = false
    private var bxColor:BXColor? = null
    private var isPlayListOpen= false
    private val fragmentInfo= FragmentInfo()
    var color: Int = 0
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mMusicService = (service as MusicPlayerService.MusicBinder).serviceInstance
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
        setViewPager()
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        buttonNext.setOnClickListener(this)
        buttonPlay.setOnClickListener(this)
        buttonPrev.setOnClickListener(this)
        preferenceHelper = PreferenceHelper(this@PlayerActivity)
        getCurrentList()


    }

    override fun onStart() {
        super.onStart()
        if (mPlaybackListener == null) {
            mPlaybackListener = PlaybackListener()
            mPlayerAdapter?.setPlaybackInfoListener(mPlaybackListener!!)

        }

    }

    override fun onPause() {
        super.onPause()
        doUnbindService()
    }

    override fun onResume() {
        super.onResume()
        doBindService()
    }
    fun setColorCallback(bxColor: BXColor){
        this@PlayerActivity.bxColor= bxColor
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
        mSectionsPagerAdapter = MainTabsAdapter(supportFragmentManager, 5,this@PlayerActivity)
        mViewPager.apply {
            adapter = mSectionsPagerAdapter
            currentItem = 2
            addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabsMain))
        }
        tabsMain.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(mViewPager))
    }

    private fun control() {
        constControls!!.setOnClickListener {
            if (isPlayingNew || mPlayerAdapter?.isPlaying()!!){
                getInfoFragment()
            }

        }
    }

    private fun getInfoFragment() {
        val fragmentTag = fragmentInfo.tag
        val popped = supportFragmentManager.popBackStackImmediate(fragmentTag, 0)
        if (!popped && supportFragmentManager.findFragmentByTag(fragmentTag) == null) {
            supportFragmentManager
                    .beginTransaction()
                    .addToBackStack(fragmentTag)
                    .add(R.id.frame_current, fragmentInfo, fragmentTag)
                    .show(fragmentInfo)
                    .commit()


        }
        frame_music.visibility = View.GONE
        constControls.visibility =View.GONE
    }
    fun replaceFragment(){
        frame_music.visibility = View.GONE
    }
    fun controls():View{
        return constControls
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

        if (isPlayListOpen){
         fragmentInfo.togglePlayList()
        }
        else if (supportFragmentManager.findFragmentByTag(Universal.ALBUM_INFO_TAG)!=null){
            supportFragmentManager.popBackStack()
            frame_music.visibility = View.VISIBLE

        }
        else if (fragments == 1) {
            supportFragmentManager.popBackStackImmediate()
            frame_music.visibility = View.VISIBLE
            constControls!!.visibility = View.VISIBLE
        } else {

            super.onBackPressed()

        }
    }
    fun togglePlaylist(isOpen:Boolean){
        isPlayListOpen= isOpen
    }
    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE )
                    != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE )
                    != PackageManager.PERMISSION_GRANTED)  {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE), 1)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode==1){
        if (grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_DENIED){
            toast("Permissions Denied")
            finish()
        }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.buttonPrev -> {
                skipPrev()
            }
            R.id.buttonPlay -> {
                if (!isPlayingNew){
                    onSongSelected(currentSongs[currentPosition], currentSongs)
                    isPlayingNew=true
                }else{
                    resumeOrPause()
                }
            }
            R.id.buttonNext -> {
                skipNext()
            }
        }
    }

    private fun getCurrentList() {
        val gson = Gson()
        val jsonText = preferenceHelper?.playingList
        val type: Type = object : TypeToken<MutableList<Song>>() {}.type
        val songs = gson.fromJson<MutableList<Song>>(jsonText, type)

        val position = preferenceHelper?.currentIndex
        currentPosition = position!!
        if (songs!=null && songs.size > 0) {
            currentSongs = songs
            val song = currentSongs[position]
            textViewArtName?.text = song.artist
            textViewTitle.text = song.title
            val retriever = MediaMetadataRetriever()
            val inputStream: InputStream?
            retriever.setDataSource(song.path)
            var image = BitmapFactory.decodeResource(resources, R.drawable.cover2)
            if (retriever.embeddedPicture != null) {
                inputStream = ByteArrayInputStream(retriever.embeddedPicture)
                image = BitmapFactory.decodeStream(inputStream)
                imageViewInfo.setImageBitmap(image)
                blurryLayout.setBitmapBlurry(image, 20, 10)
            }
            getBxColor(image)
        }
    }

    private fun onSongSelected(song: Song, songs: MutableList<Song>) {

        mPlayerAdapter!!.setCurrentSong(song, songs)
        mPlayerAdapter!!.initMediaPlayer()
        mPlayerAdapter!!.getMediaPlayer()?.start()
        mMusicService!!.startForeground(BXNotificationManager.NOTIFICATION_ID,
                mMusicNotificationManager!!.createNotification())



    }

    private fun skipPrev() {
        if (checkIsPlayer()) {
            mPlayerAdapter?.instantReset()
        }
    }

    private fun resumeOrPause() {
        if (checkIsPlayer()) {
            mPlayerAdapter?.resumeOrPause()
            updatePlayingStatus()
        }
    }

    private fun skipNext() {
        if (checkIsPlayer()) {
            mPlayerAdapter!!.skip(true)
        }
    }

    private fun doBindService() {
        startService<MusicPlayerService>()
        bindService(Intent(this@PlayerActivity,
                MusicPlayerService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
        isServiceBound = true

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


    fun updatePlaying(song: Song){
        textViewArtName?.text = song.artist
        textViewTitle?.text = song.title
        val retriever = MediaMetadataRetriever()
        val inputStream: InputStream?
        retriever.setDataSource(song.path)
        if (retriever.embeddedPicture!=null){
            inputStream = ByteArrayInputStream(retriever.embeddedPicture)
           val image =BitmapFactory.decodeStream(inputStream)
            blurryLayout?.setBitmapBlurry(image, 20, 10)
            imageViewInfo?.setImageBitmap(image)
            getBxColor(image)
        }else{
            val image =BitmapFactory.decodeResource(resources,R.drawable.cover2)
            blurryLayout?.setBitmapBlurry(image, 20, 10)
            imageViewInfo?.setImageBitmap(image)
            getBxColor(image)
        }
    }

    private fun updatePlayingInfo(restore: Boolean, startPlay: Boolean) {
        if (startPlay) {
            mPlayerAdapter?.getMediaPlayer()?.start()
            Handler().postDelayed({
                mMusicService?.startForeground(BXNotificationManager.NOTIFICATION_ID,
                        mMusicNotificationManager!!.createNotification())
            }, 250)
        }

        try {
            val selectedSong = mPlayerAdapter?.getCurrentSong()
            textViewArtName?.text = selectedSong?.artist
            textViewTitle?.text = selectedSong?.title
            toast(selectedSong?.title!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (restore) {
            updatePlayingStatus()
        }
    }

    private fun getBxColor(bitmap: Bitmap){
        color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(R.color.colorAccent, null)
        } else resources.getColor(R.color.colorAccent)
        Palette.from(bitmap).generate { palette ->
            val vibrant = palette?.lightVibrantSwatch
            try {
                color = vibrant?.rgb!!
                setViewColors(color)
                tabsMain.tabRippleColor= ColorStateList.valueOf(color)
                tabsMain.setSelectedTabIndicatorColor(color)
                bxColor?.songColor(color)
                setViewColors(color)

            } catch (e: Exception) {
            }
        }


    }

    private fun updatePlayingStatus() {
        val drawable = if (mPlayerAdapter?.getState() != PlaybackInfoListener.State.PAUSED)
            R.drawable.pause
        else
            R.drawable.play_icon
        try {
            buttonPlay.setImageDrawable(getDrawable(drawable))

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun setViewColors(color:Int) {
        buttonPlay.setColorFilter(color)
        buttonNext.setColorFilter(color)
        buttonPrev.setColorFilter(color)
    }

    internal inner class PlaybackListener : PlaybackInfoListener() {

        override fun onPositionChanged(position: Int) {
        }

        override fun onStateChanged(@State state: Int) {
            if (mPlayerAdapter?.getState() != State.RESUMED && mPlayerAdapter?.getState() != State.PAUSED) {
                updatePlayingInfo(false, startPlay = true)
            }
            if (state == PlaybackInfoListener.State.PLAYING) {
                updatePlayingInfo(restore = true, startPlay = false)

            }
        }

        override fun onPlaybackCompleted() {

        }
    }
}
