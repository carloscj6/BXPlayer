package com.revosleap.bxplayer.utils.playback

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class BxPlayerService : Service() {
    private val mIBinder = BXBinder()

    var mediaPlayerHolder: MediaPlayerHolder? = null
        private set

    var musicNotificationManager: BXNotificationManager? = null
        private set

    var isRestoredFromPause = false


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        mediaPlayerHolder!!.registerNoisyReceiver()
        return Service.START_NOT_STICKY
    }

    override fun onDestroy() {
        mediaPlayerHolder!!.registerNotificationActionsReceiver(false)
        musicNotificationManager = null
        mediaPlayerHolder!!.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        if (mediaPlayerHolder == null) {
//            mediaPlayerHolder = MediaPlayerHolder(this)
//            musicNotificationManager = BXNotificationManager(this)
//            mediaPlayerHolder!!.registerNotificationActionsReceiver(true)
        }
        return mIBinder
    }

    inner class BXBinder : Binder() {
        val instance: BxPlayerService
            get() = this@BxPlayerService
    }

}
