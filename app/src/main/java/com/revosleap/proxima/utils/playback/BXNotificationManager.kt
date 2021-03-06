package com.revosleap.proxima.utils.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
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

import com.revosleap.proxima.models.Song
import com.revosleap.proxima.utils.utils.AudioUtils
import com.revosleap.proxima.ui.activities.PlayerActivity
import com.revosleap.proxima.R
import com.revosleap.proxima.services.MusicPlayerService
import com.revosleap.proxima.utils.utils.Universal

class BXNotificationManager internal constructor(private val musicPlayerService: MusicPlayerService) {
    private val channelId = "com.revosleap.proxima.channelId"
    private val requestCode = 100
    val notificationManager: NotificationManager = musicPlayerService.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var mediaSession: MediaSessionCompat? = null
    private var mediaSessionManager: MediaSessionManager? = null
    private var transportControls: MediaControllerCompat.TransportControls? = null
    var notificationBuilder: NotificationCompat.Builder? = null
        private set
    private val context: Context

    init {
        context = musicPlayerService.application
    }

    private fun playerAction(action: String): PendingIntent {

        val pauseIntent = Intent()
        pauseIntent.action = action

        return PendingIntent.getBroadcast(musicPlayerService, requestCode, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun notificationAction(action: String): NotificationCompat.Action {

        val icon: Int

        icon = when (action) {
            PREV_ACTION -> R.drawable.previous
            PLAY_PAUSE_ACTION ->

                if (musicPlayerService.mediaPlayerHolder?.getState() != PlaybackInfoListener.State.PAUSED)
                    R.drawable.pause
                else
                    R.drawable.play_icon
            NEXT_ACTION -> R.drawable.next
            else -> R.drawable.previous
        }
        return NotificationCompat.Action.Builder(icon, action, playerAction(action)).build()
    }

    fun createNotification(): Notification {

        val song = musicPlayerService.mediaPlayerHolder?.getCurrentSong()

        notificationBuilder = NotificationCompat.Builder(musicPlayerService, channelId)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        val openPlayerIntent = Intent(musicPlayerService, PlayerActivity::class.java)
        openPlayerIntent.action =Universal.infoAction
        openPlayerIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val contentIntent = PendingIntent.getActivity(musicPlayerService, requestCode,
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
                    musicPlayerService.getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_LOW)

            notificationChannel.description = musicPlayerService.getString(R.string.app_name)

            notificationChannel.enableLights(false)
            notificationChannel.enableVibration(false)
            notificationChannel.setShowBadge(false)

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun updateMetaData(mSelectedSong: Song) {
        mediaSession = MediaSessionCompat(context, "BXPlayer")
        mediaSession!!.setMetadata(MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, AudioUtils.cover(mSelectedSong.path!!,context))
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, mSelectedSong.artist)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, mSelectedSong.albumName)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mSelectedSong.title)
                .build())
    }

    @Throws(RemoteException::class)
    private fun initMediaSession(model: Song) {
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

        // final VectorDrawable vectorDrawable = (VectorDrawable) musicPlayerService.getDrawable(R.drawable.cover2);

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
        internal const val PLAY_PAUSE_ACTION = "com.revosleap.proxima.PLAYPAUSE"
        const internal val NEXT_ACTION = "com.revosleap.proxima.NEXT"
        const internal val PREV_ACTION = "com.revosleap.proxima.PREV"
    }
}
