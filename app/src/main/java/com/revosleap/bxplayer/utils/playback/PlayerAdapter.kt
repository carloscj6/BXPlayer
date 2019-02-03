package com.revosleap.bxplayer.utils.playback

import android.app.Activity
import android.media.MediaPlayer


import com.revosleap.bxplayer.utils.models.Album
import com.revosleap.bxplayer.utils.models.AudioModel


interface PlayerAdapter {

    fun isMediaPlayer(): Boolean

    fun isPlaying(): Boolean

    fun isReset(): Boolean

    fun getCurrentSong(): AudioModel?

    fun getPlayerPosition(): Int

    fun getSelectedAlbum(): Album?

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

    fun setSelectedAlbum(album: Album)

    fun getMediaPlayer(): MediaPlayer?

    @PlaybackInfoListener.State
    fun getState(): Int
}
