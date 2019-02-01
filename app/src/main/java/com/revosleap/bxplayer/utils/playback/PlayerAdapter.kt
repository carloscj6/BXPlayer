package com.revosleap.bxplayer.utils.playback

import android.app.Activity
import android.media.MediaPlayer


import com.revosleap.bxplayer.utils.models.Album
import com.revosleap.bxplayer.utils.models.AudioModel


interface PlayerAdapter {

    val isMediaPlayer: Boolean

    val isPlaying: Boolean

    val isReset: Boolean

    val currentSong: AudioModel

    @get:PlaybackInfoListener.State
    val state: Int

    val playerPosition: Int

    var selectedAlbum: Album

    val mediaPlayer: MediaPlayer

    fun initMediaPlayer()

    fun release()

    fun resumeOrPause()

    fun reset()

    fun instantReset()

    fun skip(isNext: Boolean)

    fun openEqualizer(activity: Activity)

    fun seekTo(position: Int)

    fun setPlaybackInfoListener(playbackInfoListener: PlaybackInfoListener)

    fun registerNotificationActionsReceiver(isRegister: Boolean)

    fun setCurrentSong(song: AudioModel, songs: List<AudioModel>)

    fun onPauseActivity()

    fun onResumeActivity()
}
