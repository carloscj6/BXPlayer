package com.revosleap.bxplayer.BxUtils;

import android.app.Activity;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;

import java.util.List;

public interface BXPlayerAdapter {
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

    void setPlaybackInfoListener(final BxPlaybackListener playbackInfoListener);

    BXCurrentSong getCurrentSong();

    @BxPlaybackListener.State
    int getState();

    int getPlayerPosition();

    void registerNotificationActionsReceiver(final boolean isRegister);

    BXAlbum getSelectedAlbum();

    void setSelectedAlbum(final BXAlbum album);

    void setCurrentSong(@NonNull final BXCurrentSong song, @NonNull final List<BXCurrentSong> songs);

    MediaPlayer getMediaPlayer();

    void onPauseActivity();

    void onResumeActivity();
}
