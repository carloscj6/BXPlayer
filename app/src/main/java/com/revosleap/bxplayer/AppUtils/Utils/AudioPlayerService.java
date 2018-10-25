package com.revosleap.bxplayer.AppUtils.Utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.revosleap.bxplayer.AppUtils.Models.AudioModel;
import com.revosleap.bxplayer.PlayerActivity;
import com.revosleap.bxplayer.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AudioPlayerService extends Service implements MediaPlayer.OnCompletionListener,
MediaPlayer.OnPreparedListener,MediaPlayer.OnErrorListener,MediaPlayer.OnSeekCompleteListener,
MediaPlayer.OnInfoListener,AudioManager.OnAudioFocusChangeListener{
    public static final String ACTION_PLAY = "com.revosleap.bxplayer.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.revosleap.bxplayer.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.revosleap.bxplayer.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.revosleap.bxplayer.ACTION_NEXT";
    public static final String ACTION_STOP = "com.revosleap.bxplayer.ACTION_STOP";
    private MediaPlayer mediaPlayer;
    //mediaSession
    private MediaSessionManager sessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;
    private static final int NOTIFICATION_ID= 100;
    private int resumePosition;
    private AudioManager audioManager;
    private  final IBinder iBinder= new LocalBinder();

    private List<AudioModel> audioList;
    private int audioIndex= -1;

    private AudioModel activeAudio;
    // handle calls
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //listen to incoming calls
        callStateListener();
        //register change in audio outputs
        registerNoisyReceiver();
        // listen to new Audio to play
        registerPlayNewAudio();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {

            //Load data from SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            audioList = storage.loadAudio();
            audioIndex = storage.loadAudioIndex();

            if (audioIndex != -1 && audioIndex < audioList.size()) {
                //index is in a valid range
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }
        } catch (NullPointerException e) {
            stopSelf();
        }

        //Request audio focus
        if (requestAudioFocus() == false) {
            //Could not gain focus
            stopSelf();
        }

        if (sessionManager == null) {
            try {
                initMediaSession();
                initMediaPlayer();
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
            buildNotification(PlaybackStatus.PLAYING);
        }

        //Handle Intent action from MediaSession.TransportControls
        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mediaSession.release();
        removeNotification();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
        }
        removeAudioFocus();
        //Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        removeNotification();

        //unregister BroadcastReceivers
        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(playNewAudio);

        //clear cached playlist
        new StorageUtil(getApplicationContext()).clearCachedAudioPlaylist();
    }

    @Override
    public void onAudioFocusChange(int i) {

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        stopMedia();

        removeNotification();
        //stop the service
        stopSelf();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {

    }
    private void initMediaSession() throws RemoteException {
        if (sessionManager != null) return; //mediaSessionManager exists

        sessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        // Create a new MediaSession
        mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
        //Get MediaSessions transport controls
        transportControls = mediaSession.getController().getTransportControls();
        //set MediaSession -> ready to receive media commands
        mediaSession.setActive(true);
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        //Set mediaSession's MetaData
        updateMetaData();

        // Attach Callback to receive MediaSession updates
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            // Implement callbacks
            @Override
            public void onPlay() {
                super.onPlay();

                resumeMedia();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onPause() {
                super.onPause();

                pauseMedia();
                buildNotification(PlaybackStatus.PAUSED);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();

                skipToNext();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();

                skipToPrevious();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onStop() {
                super.onStop();
                removeNotification();
                //Stop the service
                stopSelf();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
            }
        });
    }
    private void skipToNext() {

        if (audioIndex == audioList.size() - 1) {
            //if last in playlist
            audioIndex = 0;
            activeAudio = audioList.get(audioIndex);
        } else {
            //get next in playlist
            activeAudio = audioList.get(++audioIndex);
        }

        //Update stored index
        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        stopMedia();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();
    }

    private void skipToPrevious() {

        if (audioIndex == 0) {
            //if first in playlist
            //set index to the last of audioList
            audioIndex = audioList.size() - 1;
            activeAudio = audioList.get(audioIndex);
        } else {
            //get previous in playlist
            activeAudio = audioList.get(--audioIndex);
        }

        //Update stored index
        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        stopMedia();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();
    }
    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }

    public class LocalBinder extends Binder {
        public AudioPlayerService getService(){
            return AudioPlayerService.this;
        }
    }
    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        }
    }
    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            pauseMedia();
            buildNotification(PlaybackStatus.PAUSED);
        }
    };
    private void registerNoisyReceiver() {
        IntentFilter intentFilter= new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver,intentFilter);
    }
    private void callStateListener() {
            telephonyManager= (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

            phoneStateListener= new PhoneStateListener(){
                @Override
                public void onCallStateChanged (int state, String incomingNumber){
                    switch (state){
                        case TelephonyManager.CALL_STATE_OFFHOOK:
                        case TelephonyManager.CALL_STATE_RINGING:
                            if (mediaPlayer!= null){
                                pauseMedia();
                                ongoingCall= true;
                            }
                            break;
                        case TelephonyManager.CALL_STATE_IDLE:
                            if (mediaPlayer!= null){
                                if (ongoingCall){
                                    ongoingCall= false;
                                    resumeMedia();
                                }
                            }
                            break;
                    }
                }
            };
    }
    private void pauseMedia(){
        if (mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        }
    }
    private void resumeMedia(){
        if (mediaPlayer.isPlaying()){
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
    }
    private void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void stopMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }
    private void initMediaPlayer(){
        if (mediaPlayer== null){
            mediaPlayer= new MediaPlayer();
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnPreparedListener(this);
           // mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setOnSeekCompleteListener(this);
            mediaPlayer.setOnInfoListener(this);
            //Reset so that the MediaPlayer is not pointing to another data source
            mediaPlayer.reset();


            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mediaPlayer.setDataSource(activeAudio.getPath());
            }
            catch (IOException e){
                e.printStackTrace();
                stopSelf();
            }
            mediaPlayer.prepareAsync();
        }
    }
    private void updateMetaData(){
        Bitmap albumArt= BitmapFactory.decodeResource(getResources(), R.drawable.cover2);
        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
        .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,albumArt)
        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST,activeAudio.getArtist())
        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM,activeAudio.getAlbum())
        .putString(MediaMetadataCompat.METADATA_KEY_TITLE,activeAudio.getTitle())
        .build());
    }
    private void buildNotification(PlaybackStatus playbackStatus){
        /**
         * Notification actions -> playbackAction()
         *  0 -> Play
         *  1 -> Pause
         *  2 -> Next track
         *  3 -> Previous track
         */
        int notificationAction= android.R.drawable.ic_media_pause;
        PendingIntent playPuaseAction= null;
        if (playbackStatus==PlaybackStatus.PLAYING){
            notificationAction = R.drawable.pause;
            playPuaseAction= playbackAction(0);
        }
        else if (playbackStatus==PlaybackStatus.PAUSED){
            notificationAction = R.drawable.play_icon;
            //create the play action
            playPuaseAction = playbackAction(0);
        }
        Bitmap largeIcon= BitmapFactory.decodeResource(getResources(),R.drawable.cover2);
        NotificationCompat.Builder notificationBuilder= new NotificationCompat.Builder(this)
                .setShowWhen(false)
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.getSessionToken())
                .setShowActionsInCompactView(0,1,2))
                .setColor(getResources().getColor(R.color.colorAccentLight))
                .setLargeIcon(largeIcon)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_if_speaker)
                .setContentText(activeAudio.getArtist())
                .setContentTitle(activeAudio.getAlbum())
                .setContentInfo(activeAudio.getTitle())
                .setOngoing(true)
                // Add playback actions
                .addAction(R.drawable.previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", playPuaseAction)
                .addAction(R.drawable.next, "next", playbackAction(2));
        ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(NOTIFICATION_ID,notificationBuilder.build());


    }
    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, AudioPlayerService.class);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                // Next track
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                // Previous track
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }
    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Get the new media index form SharedPreferences
            audioIndex = new StorageUtil(getApplicationContext()).loadAudioIndex();
            if (audioIndex != -1 && audioIndex < audioList.size()) {
                //index is in a valid range
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }

            //A PLAY_NEW_AUDIO action received
            //reset mediaPlayer to play the new Audio
            stopMedia();
            mediaPlayer.reset();
            initMediaPlayer();
            updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);
        }
    };
    private void registerPlayNewAudio(){
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(PlayerActivity.Broadcast_PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio, filter);
    }
}
