package com.revosleap.bxplayer.ui.fragments


import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.graphics.Palette
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import com.revosleap.bxplayer.R
import com.revosleap.bxplayer.callbacks.BXColor
import com.revosleap.bxplayer.services.MusicPlayerService
import com.revosleap.bxplayer.ui.activities.PlayerActivity
import com.revosleap.bxplayer.utils.playback.BXNotificationManager
import com.revosleap.bxplayer.utils.playback.PlaybackInfoListener
import com.revosleap.bxplayer.utils.playback.PlayerAdapter
import com.revosleap.bxplayer.utils.utils.AudioUtils
import com.revosleap.bxplayer.utils.utils.EqualizerUtils
import kotlinx.android.synthetic.main.info.*
import org.jetbrains.anko.startService
import org.jetbrains.anko.toast
import java.io.ByteArrayInputStream
import java.io.InputStream


class InfoFragment : Fragment(), View.OnClickListener,BXColor {
    private var mUserIsSeeking: Boolean = false
    private var mSelectedArtist: String? = null
    private var mPlayerAdapter: PlayerAdapter? = null
    private var mMusicService: MusicPlayerService? = null
    private var mMusicNotificationManager: BXNotificationManager? = null
    private var mPlaybackListener: PlaybackListener? = null
    private var playerActivity:PlayerActivity?=null
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        getService()
        playerActivity =activity!!as PlayerActivity
        playerActivity?.setColorCallback(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClickListeners()
        initializeSeekBar()
        mPlaybackListener?.onStateChanged(PlaybackInfoListener.State.PLAYING)
    }

    override fun onResume() {
        super.onResume()
        getService()
        activity?.startService<MusicPlayerService>()
        mPlayerAdapter!!.getMediaPlayer()?.start()
        mMusicService!!.startForeground(BXNotificationManager.NOTIFICATION_ID,
                mMusicNotificationManager!!.createNotification())
    }

    override fun songColor(color: Int) {
        buttonInfoPlaylist?.setColorFilter(color)
        buttonInfoPlay?.setColorFilter(color)
        buttonInfoAll?.setColorFilter(color)
        buttonInfoFave?.setColorFilter(color)
        buttonInfoVol?.setColorFilter(color)
        seekBarInfo?.progressBackgroundTintList = ColorStateList.valueOf(color)
        buttonInfoShuffle?.setColorFilter(color)
        buttonInfoNext?.setColorFilter(color)
        buttonInfoPrev?.setColorFilter(color)
    }
    private fun getBxColor(bitmap: Bitmap){
        var color:Int
        color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity?.resources!!.getColor(R.color.colorAccent, null)
        } else activity?.resources!!.getColor(R.color.colorAccent)
        Palette.from(bitmap).generate { palette ->
            val vibrant = palette?.lightVibrantSwatch
            try {
                color = vibrant?.rgb!!
               songColor(color)
                playerActivity?.setViewColors(color)
            } catch (e: Exception) {
            }
        }


    }
    private fun getService(){
        val playerActivity = activity as PlayerActivity
        mMusicService = playerActivity.getPlayerService()
        if (mMusicService != null) {
            mPlayerAdapter = mMusicService!!.mediaPlayerHolder
            mMusicNotificationManager = mMusicService!!.musicNotificationManager
        }
        if (mPlaybackListener == null) {
            mPlaybackListener = PlaybackListener()
            mPlayerAdapter?.setPlaybackInfoListener(mPlaybackListener!!)
        }
    }
    private fun initializeSeekBar() {
        seekBarInfo.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            var userSelectedPosition = 0

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    userSelectedPosition = progress

                }
                textViewProgress?.text = AudioUtils.formatDuration(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                mUserIsSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (mUserIsSeeking) {
                    //  mSongPosition.setTextColor(currentPositionColor);
                }
                mUserIsSeeking = false
                mPlayerAdapter!!.seekTo(userSelectedPosition)
            }
        })
    }

    private fun updatePlayingStatus() {
        val drawable = if (mPlayerAdapter?.getState() != PlaybackInfoListener.State.PAUSED)
            R.drawable.pause
        else
            R.drawable.play_icon
        try {
            buttonInfoPlay.setImageDrawable(activity?.getDrawable(drawable))

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun updatePlayingInfo(restore: Boolean, startPlay: Boolean) {


        if (startPlay) {
            mPlayerAdapter?.getMediaPlayer()?.start()
            Handler().postDelayed({
                mMusicService!!.startForeground(BXNotificationManager.NOTIFICATION_ID,
                        mMusicNotificationManager!!.createNotification())
            }, 250)
        }

        val selectedSong = mPlayerAdapter?.getCurrentSong()
        mSelectedArtist = selectedSong?.artist
        val duration = selectedSong?.duration
        seekBarInfo?.max = duration!!
        textViewDuration?.text = AudioUtils.formatDuration(duration)
        textViewInfoTitle?.text = selectedSong.title
        textViewInfoArtist?.text = selectedSong.artist
        val retriever = MediaMetadataRetriever()
        val inputStream: InputStream?
        retriever.setDataSource(selectedSong?.path)

        if (retriever.embeddedPicture != null) {
            inputStream = ByteArrayInputStream(retriever.embeddedPicture)
            val image =BitmapFactory.decodeStream(inputStream)
            imageViewInfo?.setImageBitmap(image)
            try {
                playerActivity?.updatePlaying(selectedSong)
                getBxColor(image)
            } catch (e: Exception) {
            }

        }else{
            val image = BitmapFactory.decodeResource(playerActivity?.resources!!,R.drawable.cover2)
            imageViewInfo?.setImageBitmap(image)
            try {
                playerActivity?.updatePlaying(selectedSong)
                getBxColor(image)
            } catch (e: Exception) {
            }
        }


        if (restore) {
            seekBarInfo?.progress = mPlayerAdapter?.getPlayerPosition()!!
            updatePlayingStatus()

        }

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.buttonInfoPlaylist -> {
            }
            R.id.buttonInfoFave -> {
            }
            R.id.buttonInfoVol -> {
            }
            R.id.buttonInfoShuffle -> {
            }
            R.id.buttonInfoPrev -> skipPrev()
            R.id.buttonInfoPlay -> resumeOrPause()
            R.id.buttonInfoNext -> skipNext()
            R.id.buttonInfoAll -> {
            }
        }
    }

    private fun setClickListeners() {
        buttonInfoAll.setOnClickListener(this)
        buttonInfoFave.setOnClickListener(this)
        buttonInfoNext.setOnClickListener(this)
        buttonInfoPlay.setOnClickListener(this)
        buttonInfoPlaylist.setOnClickListener(this)
        buttonInfoPrev.setOnClickListener(this)
        buttonInfoShuffle.setOnClickListener(this)
        buttonInfoVol.setOnClickListener(this)
    }

    internal inner class PlaybackListener : PlaybackInfoListener() {

        override fun onPositionChanged(position: Int) {
            if (!mUserIsSeeking) {
                seekBarInfo?.progress = position
            }
        }

        override fun onStateChanged(@State state: Int) {
            updatePlayingStatus()
            if (mPlayerAdapter?.getState() != PlaybackInfoListener.State.RESUMED && mPlayerAdapter?.getState() != PlaybackInfoListener.State.PAUSED) {
                updatePlayingInfo(restore = false, startPlay = true)
            }
            if (state == PlaybackInfoListener.State.PLAYING) {
                updatePlayingInfo(restore = true, startPlay = false)

            }
        }

        override fun onPlaybackCompleted() {
            //updateResetStatus(true);
        }
    }

    private fun checkIsPlayer(): Boolean {

        val isPlayer = mPlayerAdapter?.isMediaPlayer()
        if (!isPlayer!!) {
            EqualizerUtils.notifyNoSessionId(activity!!)
        }
        return isPlayer
    }

    private fun skipPrev() {
        if (checkIsPlayer()) {
            mPlayerAdapter?.instantReset()
        }
    }

    private fun resumeOrPause() {
        if (checkIsPlayer()) {
            mPlayerAdapter?.resumeOrPause()
        }
    }

    private fun skipNext() {
        if (checkIsPlayer()) {
            mPlayerAdapter!!.skip(true)
        }
    }

    fun openEqualizer() {
        if (EqualizerUtils.hasEqualizer(activity!!)) {
            if (checkIsPlayer()) {
                mPlayerAdapter?.openEqualizer(activity!!)
            }
        } else {
            Toast.makeText(activity, "No equalizer found", Toast.LENGTH_SHORT).show()
        }
    }


}
