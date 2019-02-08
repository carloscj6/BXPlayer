package com.revosleap.bxplayer.callbacks


import android.app.Activity
import android.media.MediaPlayer
import com.revosleap.bxplayer.models.Album
import com.revosleap.bxplayer.models.Song
import com.revosleap.bxplayer.utils.playback.PlaybackInfoListener


interface PlayerAdapter {

    fun isMediaPlayer(): Boolean

    fun isPlaying(): Boolean

    fun isReset(): Boolean

    fun getCurrentSong(): Song?

    fun getCurrentSongs():MutableList<Song>?

    fun getPlayerPosition(): Int

    fun getSelectedAlbum(): Album?

    fun initMediaPlayer()

    fun shufflePlayList()

    fun release()

    fun resumeOrPause()

    fun reset()

    fun instantReset()

    fun skip(isNext: Boolean)

    fun openEqualizer(activity: Activity)

    fun seekTo(position: Int)

    fun setPlaybackInfoListener(playbackInfoListener: PlaybackInfoListener)

    fun registerNotificationActionsReceiver(isRegister: Boolean)

    fun setCurrentSong(song: Song, songs: MutableList<Song>)

    fun onPauseActivity()

    fun onResumeActivity()

    fun setSelectedAlbum(album: Album)

    fun getMediaPlayer(): MediaPlayer?

    @PlaybackInfoListener.State
    fun getState(): Int
}
