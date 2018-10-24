package com.revosleap.bxplayer.BxUtils;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class BxAudioService extends Service{
    private final IBinder mIBinder = new LocalBinder();
    private BXMediaPlayerHolder mediaPlayerHolder;
    private BxNotificationManager notificationManager;
    private boolean sRestoredFromPause = false;

    public final boolean isRestoredFromPause() {
        return sRestoredFromPause;
    }

    public void setRestoredFromPause(boolean restore) {
        sRestoredFromPause = restore;
    }
    public final BXMediaPlayerHolder getMediaPlayerHolder() {
        return mediaPlayerHolder;
    }

    public BxNotificationManager getMusicNotificationManager() {
        return notificationManager;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        notificationManager=null;
        mediaPlayerHolder.registerNotificationActionsReceiver(false);
        mediaPlayerHolder.release();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (mediaPlayerHolder==null){
        mediaPlayerHolder= new BXMediaPlayerHolder(this);
        notificationManager= new BxNotificationManager(this);
        mediaPlayerHolder.registerNotificationActionsReceiver(true);
        }
        return mIBinder;
    }
    public class LocalBinder extends Binder{
        public BxAudioService getInstance(){
            return BxAudioService.this;
        }
    }
}
