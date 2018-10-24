package com.revosleap.bxplayer.BxUtils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.Spanned;

import com.revosleap.bxplayer.AppUtils.Player.Utils;
import com.revosleap.bxplayer.PlayerActivity;
import com.revosleap.bxplayer.R;

public class BxNotificationManager {
    public static final int NOTIFICATION_ID = 1010;
    static final String PLAY_PAUSE_ACTION = "PLAYPAUSE";
    static final String NEXT_ACTION = "NEXT";
    static final String PREV_ACTION = "PREV";
    private final String CHANNEL_ID = "com.revosleap.bxplayer.CHANNEL_ID";
    private final int REQUEST_CODE = 1800;
    private final NotificationManager mNotificationManager;
    private final BxAudioService mMusicService;
    private NotificationCompat.Builder mNotificationBuilder;
    private int mAccent;
    BxNotificationManager(@NonNull final BxAudioService musicService) {
        mMusicService = musicService;
        mNotificationManager = (NotificationManager) mMusicService.getSystemService(Context.NOTIFICATION_SERVICE);
    }
    public void setAccentColor(final int color) {
        mAccent = ContextCompat.getColor(mMusicService, color);
    }
    public final NotificationManager getNotificationManager() {
        return mNotificationManager;
    }

    public final NotificationCompat.Builder getNotificationBuilder() {
        return mNotificationBuilder;
    }
    private PendingIntent playerAction(String action) {

        final Intent pauseIntent = new Intent();
        pauseIntent.setAction(action);

        return PendingIntent.getBroadcast(mMusicService, REQUEST_CODE, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    public Notification notification(){
        final BXCurrentSong song = mMusicService.getMediaPlayerHolder().getCurrentSong();
        mNotificationBuilder = new NotificationCompat.Builder(mMusicService, CHANNEL_ID);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        final Intent openPlayerIntent = new Intent(mMusicService, PlayerActivity.class);
        //TODO 1 make fragment pending
        openPlayerIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent contentIntent = PendingIntent.getActivity(mMusicService, REQUEST_CODE,
                openPlayerIntent, 0);

        final String artist = song.artistName;
        final String songTitle = song.title;

        final Spanned spanned = Utils.buildSpanned(mMusicService.getString(R.string.playing_song, artist, songTitle));
        //TODO check later
        mNotificationBuilder
                .setShowWhen(false)
                .setSmallIcon(R.drawable.play_icon)
                .setLargeIcon(getLargeIcon())
                .setColor(mAccent)
                .setContentTitle(spanned)
                .setContentText(song.albumName)
                .setContentIntent(contentIntent)
                .addAction(notificationAction(PREV_ACTION))
                .addAction(notificationAction(PLAY_PAUSE_ACTION))
                .addAction(notificationAction(NEXT_ACTION))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        mNotificationBuilder.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2));
        return mNotificationBuilder.build();
    }
    @NonNull
    private NotificationCompat.Action notificationAction(final String action) {

        int icon;

        switch (action) {
            default:
            case PREV_ACTION:
                icon = R.drawable.previous;
                break;
            case PLAY_PAUSE_ACTION:

                icon = mMusicService.getMediaPlayerHolder().getState() != BxPlaybackListener.State.PAUSED ?
                        R.drawable.pause : R.drawable.play_icon;
                break;
            case NEXT_ACTION:
                icon = R.drawable.next;
                break;
        }
        return new NotificationCompat.Action.Builder(icon, action, playerAction(action)).build();
    }
    @RequiresApi(26)
    private void createNotificationChannel() {

        if (mNotificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            final NotificationChannel notificationChannel =
                    new NotificationChannel(CHANNEL_ID,
                            mMusicService.getString(R.string.app_name),
                            NotificationManager.IMPORTANCE_LOW);

            notificationChannel.setDescription(
                    mMusicService.getString(R.string.app_name));

            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);
            notificationChannel.setShowBadge(false);

            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }
    private Bitmap getLargeIcon() {

        final VectorDrawable vectorDrawable = (VectorDrawable) mMusicService.getDrawable(R.drawable.play_icon);

        final int largeIconSize = mMusicService.getResources().getDimensionPixelSize(R.dimen.notification_large_dim);
        final Bitmap bitmap = Bitmap.createBitmap(largeIconSize, largeIconSize, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);

        if (vectorDrawable != null) {
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.setTint(mAccent);
            vectorDrawable.setAlpha(100);
            vectorDrawable.draw(canvas);
        }

        return bitmap;
    }

}
