package com.revosleap.bxplayer.AppUtils.BxPlayback;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

public class BxPlayerService extends Service {
    private final IBinder mIBinder = new BXBinder();

    private MediaPlayerHolder mMediaPlayerHolder;

    private BXNotificationManager mMusicNotificationManager;

    private boolean sRestoredFromPause = false;

    public final boolean isRestoredFromPause() {
        return sRestoredFromPause;
    }

    public void setRestoredFromPause(boolean restore) {
        sRestoredFromPause = restore;
    }

    public final MediaPlayerHolder getMediaPlayerHolder() {
        return mMediaPlayerHolder;
    }

    public BXNotificationManager getMusicNotificationManager() {
        return mMusicNotificationManager;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mMediaPlayerHolder.registerNoisyReceiver();
        return START_NOT_STICKY;
    }
    @Override
    public void onDestroy() {
        mMediaPlayerHolder.registerNotificationActionsReceiver(false);
        mMusicNotificationManager = null;
        mMediaPlayerHolder.release();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (mMediaPlayerHolder == null) {
            mMediaPlayerHolder = new MediaPlayerHolder(this);
            mMusicNotificationManager = new BXNotificationManager(this);
            mMediaPlayerHolder.registerNotificationActionsReceiver(true);
        }
        return mIBinder;
    }
    public class BXBinder extends Binder{
        public BxPlayerService getInstance(){
            return BxPlayerService.this;
        }
    }

}
