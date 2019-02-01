package com.revosleap.bxplayer.utils.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.media.session.MediaSessionManager
import android.os.Build
import android.os.RemoteException
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat

import com.revosleap.bxplayer.utils.models.AudioModel
import com.revosleap.bxplayer.utils.utils.AudioUtils
import com.revosleap.bxplayer.ui.activities.PlayerActivity
import com.revosleap.bxplayer.R
import java.io.InputStream

class BXNotificationManager internal constructor(private val bxPlayerService: BxPlayerService) {
    private val channelId = "com.revosleap.bxplayer.channelId"
    private val requestCode = 100
    val notificationManager: NotificationManager = bxPlayerService.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var mediaSession: MediaSessionCompat? = null
    private var mediaSessionManager: MediaSessionManager? = null
    private var transportControls: MediaControllerCompat.TransportControls? = null
    var notificationBuilder: NotificationCompat.Builder? = null
        private set
    private val context: Context

    init {
        context = bxPlayerService.application
    }

    private fun playerAction(action: String): PendingIntent {

        val pauseIntent = Intent()
        pauseIntent.action = action

        return PendingIntent.getBroadcast(bxPlayerService, requestCode, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun notificationAction(action: String): NotificationCompat.Action {

        val icon: Int

        when (action) {
            PREV_ACTION -> icon = R.drawable.previous
            PLAY_PAUSE_ACTION ->

                icon = if (bxPlayerService.mediaPlayerHolder?.state != PlaybackInfoListener.State.PAUSED)
                    R.drawable.pause
                else
                    R.drawable.play_icon
            NEXT_ACTION -> icon = R.drawable.next
            else -> icon = R.drawable.previous
        }
        return NotificationCompat.Action.Builder(icon, action, playerAction(action)).build()
    }

    fun createNotification(): Notification {

        val song = bxPlayerService.mediaPlayerHolder?.currentSong

        notificationBuilder = NotificationCompat.Builder(bxPlayerService, channelId)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        val openPlayerIntent = Intent(bxPlayerService, PlayerActivity::class.java)
        openPlayerIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val contentIntent = PendingIntent.getActivity(bxPlayerService, requestCode,
                openPlayerIntent, 0)
        updateMetaData(song!!)
        val artist = song.artist
        val songTitle = song.title
        notificationBuilder!!
                .setShowWhen(false)
                .setSmallIcon(R.drawable.ic_if_speaker)
                .setLargeIcon(AudioUtils.cover(song.path!!,context))
                .setColor(context.resources.getColor(R.color.colorAccentLight))
                .setContentTitle(songTitle)
                .setContentText(artist)
                .setContentIntent(contentIntent)
                .addAction(notificationAction(PREV_ACTION))
                .addAction(notificationAction(PLAY_PAUSE_ACTION))
                .addAction(notificationAction(NEXT_ACTION))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        notificationBuilder!!.setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2)
                .setMediaSession(mediaSession!!.sessionToken))
        return notificationBuilder!!.build()
    }

    @RequiresApi(26)
    private fun createNotificationChannel() {

        if (notificationManager.getNotificationChannel(channelId) == null) {
            val notificationChannel = NotificationChannel(channelId,
                    bxPlayerService.getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_LOW)

            notificationChannel.description = bxPlayerService.getString(R.string.app_name)

            notificationChannel.enableLights(false)
            notificationChannel.enableVibration(false)
            notificationChannel.setShowBadge(false)

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun updateMetaData(mSelectedSong: AudioModel) {
        mediaSession = MediaSessionCompat(context, "BXPlayer")
        mediaSession!!.setMetadata(MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, AudioUtils.cover(mSelectedSong.path!!,context))
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, mSelectedSong.artist)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, mSelectedSong.album)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mSelectedSong.title)
                .build())
    }

    @Throws(RemoteException::class)
    private fun initMediaSession(model: AudioModel) {
        if (mediaSessionManager != null) return  //mediaSessionManager exists

        mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        // Create a new MediaSession
        mediaSession = MediaSessionCompat(context.applicationContext, "AudioPlayer")
        //Get MediaSessions transport controls
        transportControls = mediaSession!!.controller.transportControls
        //set MediaSession -> ready to receive media commands
        mediaSession!!.isActive = true
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession!!.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        updateMetaData(model)
        //Set mediaSession's MetaData


    }

    private fun getLargeIcon(image: Bitmap): Bitmap {

        // final VectorDrawable vectorDrawable = (VectorDrawable) bxPlayerService.getDrawable(R.drawable.cover2);

        val largeIconSize = context.resources.getDimensionPixelSize(R.dimen.notification_large_dim)
        val map = Bitmap.createBitmap(largeIconSize, largeIconSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(map)
        canvas.drawColor(Color.TRANSPARENT)
        val bitmap = image.copy(Bitmap.Config.ARGB_8888, true)
        val height = bitmap.height
        val width = bitmap.width
        bitmap.width = width * 5 / 10
        bitmap.height = height * 5 / 10
        canvas.drawBitmap(map, 2f, 2f, null)

        return Bitmap.createScaledBitmap(image, 50, 50, true)
    }

    interface BitmapColors {
        fun setColors(color: Int)
    }

    companion object {
        const val NOTIFICATION_ID = 101
        const internal val PLAY_PAUSE_ACTION = "com.revosleap.bxplayer.PLAYPAUSE"
        const internal val NEXT_ACTION = "com.revosleap.bxplayer.NEXT"
        const internal val PREV_ACTION = "com.revosleap.bxplayer.PREV"
    }
}
