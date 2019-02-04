package com.revosleap.bxplayer.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.revosleap.bxplayer.utils.playback.BXNotificationManager
import com.revosleap.bxplayer.utils.playback.MediaPlayerHolder

class MusicPlayerService : Service() {
    private val binder= MusicBinder()
    var mediaPlayerHolder: MediaPlayerHolder? = null
        private set
    var musicNotificationManager: BXNotificationManager? = null
        private set
    var isRestoredFromPause = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mediaPlayerHolder?.registerNoisyReceiver()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        if (mediaPlayerHolder == null) {
            mediaPlayerHolder = MediaPlayerHolder(this)
            musicNotificationManager = BXNotificationManager(this)
            mediaPlayerHolder!!.registerNotificationActionsReceiver(true)
        }
       return binder
    }

    override fun onDestroy() {
        mediaPlayerHolder!!.registerNotificationActionsReceiver(false)
        musicNotificationManager = null
        mediaPlayerHolder!!.release()
        super.onDestroy()
    }
    inner class MusicBinder:Binder(){
        val serviceInstance: MusicPlayerService
        get() = this@MusicPlayerService
    }
}
