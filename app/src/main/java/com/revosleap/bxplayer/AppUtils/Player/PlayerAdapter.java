package com.revosleap.bxplayer.AppUtils.Player;

import android.app.Activity;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;

import com.revosleap.bxplayer.AppUtils.Models.Album;
import com.revosleap.bxplayer.AppUtils.Models.Song;

import java.util.List;

public interface PlayerAdapter {
    void initMediaPlayer();

    void release();

    boolean isMediaPlayer();

    boolean isPlaying();

    void resumeOrPause();

    void reset();

    boolean isReset();

    void instantReset();

    void skip(final boolean isNext);

    void openEqualizer(@NonNull final Activity activity);

    void seekTo(final int position);

    void setPlaybackInfoListener(final PlaybackInfoListener playbackInfoListener);

    Song getCurrentSong();

    @PlaybackInfoListener.State
    int getState();

    int getPlayerPosition();

    void registerNotificationActionsReceiver(final boolean isRegister);

    Album getSelectedAlbum();

    void setSelectedAlbum(final Album album);

    void setCurrentSong(@NonNull final Song song, @NonNull final List<Song> songs);

    MediaPlayer getMediaPlayer();

    void onPauseActivity();

    void onResumeActivity();
}
