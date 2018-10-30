package com.revosleap.bxplayer.AppUtils.BxPlayback;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.VectorDrawable;
import android.media.session.MediaSessionManager;
import android.os.Build;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.Spanned;

import com.revosleap.bxplayer.AppUtils.Models.AudioModel;
import com.revosleap.bxplayer.AppUtils.Utils.AudioUtils;
import com.revosleap.bxplayer.Fragments.InfoFragment;
import com.revosleap.bxplayer.PlayerActivity;
import com.revosleap.bxplayer.R;

public class BXNotificationManager {
    public static final int NOTIFICATION_ID = 101;
    static final String PLAY_PAUSE_ACTION = "com.revosleap.bxplayer.PLAYPAUSE";
    static final String NEXT_ACTION = "com.revosleap.bxplayer.NEXT";
    static final String PREV_ACTION = "com.revosleap.bxplayer.PREV";
    private final String CHANNEL_ID = "com.revosleap.bxplayer.CHANNEL_ID";
    private final int REQUEST_CODE = 100;
    private final NotificationManager notificationManager;
    private MediaSessionCompat mediaSession;
    private MediaSessionManager mediaSessionManager;
    private MediaControllerCompat.TransportControls transportControls;
    private final BxPlayerService bxPlayerService;
    private NotificationCompat.Builder builder;
    private Context context;
    BXNotificationManager(final BxPlayerService service){
        bxPlayerService=service;
        notificationManager=(NotificationManager)bxPlayerService.getSystemService(Context.NOTIFICATION_SERVICE);
        context=service.getApplication();
    }
    public final NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public final NotificationCompat.Builder getNotificationBuilder() {
        return builder;
    }
    private PendingIntent playerAction(String action) {

        final Intent pauseIntent = new Intent();
        pauseIntent.setAction(action);

        return PendingIntent.getBroadcast(bxPlayerService, REQUEST_CODE, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
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

                icon = bxPlayerService.getMediaPlayerHolder().getState() != PlaybackInfoListener.State.PAUSED
                        ? R.drawable.pause : R.drawable.play_icon;
                break;
            case NEXT_ACTION:
                icon = R.drawable.next;
                break;
        }
        return new NotificationCompat.Action.Builder(icon, action, playerAction(action)).build();
    }
    public Notification createNotification() {

        final AudioModel song = bxPlayerService.getMediaPlayerHolder().getCurrentSong();

        builder = new NotificationCompat.Builder(bxPlayerService, CHANNEL_ID);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        final Intent openPlayerIntent = new Intent(bxPlayerService, PlayerActivity.class);
        openPlayerIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent contentIntent = PendingIntent.getActivity(bxPlayerService, REQUEST_CODE,
                openPlayerIntent, 0);
        updateMetaData(song);
        final String artist = song.getArtist();
        final String songTitle = song.getTitle();
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.cover2);
        Bitmap cover;
        if (AudioUtils.cover(song.getPath())!=null){
            cover=AudioUtils.cover(song.getPath());
        }else cover=largeIcon;

      //  final Spanned spanned = Utils.buildSpanned(mMusicService.getString(R.string.playing_song, artist, songTitle));

        builder
                .setShowWhen(false)
                .setSmallIcon(R.drawable.ic_if_speaker)
                .setLargeIcon(cover)
                .setColor(context.getResources().getColor(R.color.colorAccentLight))
                .setContentTitle(songTitle)
                .setContentText(artist)
                .setContentIntent(contentIntent)
                .addAction(notificationAction(PREV_ACTION))
                .addAction(notificationAction(PLAY_PAUSE_ACTION))
                .addAction(notificationAction(NEXT_ACTION))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        builder.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2)
        .setMediaSession(mediaSession.getSessionToken()));
        return builder.build();
    }
    @RequiresApi(26)
    private void createNotificationChannel() {

        if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            final NotificationChannel notificationChannel =
                    new NotificationChannel(CHANNEL_ID,
                            bxPlayerService.getString(R.string.app_name),
                            NotificationManager.IMPORTANCE_LOW);

            notificationChannel.setDescription(
                    bxPlayerService.getString(R.string.app_name));

            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);
            notificationChannel.setShowBadge(false);

            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
    private void updateMetaData(AudioModel mSelectedSong) {
        mediaSession =new MediaSessionCompat(context,"BXPlayer");
        Bitmap cover;
        Bitmap albumArt = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.cover2); //replace with medias albumArt
        // Update the current metadata
        if (AudioUtils.cover(mSelectedSong.getPath())!=null){
            cover=AudioUtils.cover(mSelectedSong.getPath());
        }else cover=albumArt;
        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, cover)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, mSelectedSong.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, mSelectedSong.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mSelectedSong.getTitle())
                .build());
    }
    private void initMediaSession(AudioModel model) throws RemoteException {
        if (mediaSessionManager != null) return; //mediaSessionManager exists

        mediaSessionManager = (MediaSessionManager)context. getSystemService(Context.MEDIA_SESSION_SERVICE);
        // Create a new MediaSession
        mediaSession = new MediaSessionCompat(context.getApplicationContext(), "AudioPlayer");
        //Get MediaSessions transport controls
        transportControls = mediaSession.getController().getTransportControls();
        //set MediaSession -> ready to receive media commands
        mediaSession.setActive(true);
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        updateMetaData(model);
        //Set mediaSession's MetaData


    }

    private Bitmap getLargeIcon(Bitmap image) {

       // final VectorDrawable vectorDrawable = (VectorDrawable) bxPlayerService.getDrawable(R.drawable.cover2);

        final int largeIconSize = context.getResources().getDimensionPixelSize(R.dimen.notification_large_dim);
        Bitmap map = Bitmap.createBitmap(largeIconSize, largeIconSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(map);
        canvas.drawColor(Color.TRANSPARENT);
        Bitmap bitmap= image.copy(Bitmap.Config.ARGB_8888,true);
        int height=bitmap.getHeight();
        int width= bitmap.getWidth();
        bitmap.setWidth(width*5/10);
        bitmap.setHeight(height*5/10);
        canvas.drawBitmap(map,2,2,null);
        Bitmap resized= Bitmap.createScaledBitmap(image,50,50,true);

        return resized;
    }
}
