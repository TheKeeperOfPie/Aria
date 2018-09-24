package com.winsonchiu.aria.media

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.session.MediaButtonReceiver
import com.winsonchiu.aria.BuildConfig
import com.winsonchiu.aria.R
import com.winsonchiu.aria.main.MainActivity
import com.winsonchiu.aria.media.util.toMediaMetadata
import com.winsonchiu.aria.queue.MediaQueue
import com.winsonchiu.aria.queue.QueueEntry

class MediaNotificationManager(
        private val service: Service
) {

    companion object {
        private val TAG = MediaNotificationManager::class.java.canonicalName

        private const val CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel.media_session"
        private const val REQUEST_CODE = 7
        const val NOTIFICATION_ID = 77
    }

    private val systemNotificationManager =
            service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val notificationManager = NotificationManagerCompat.from(service)

    private val intentPlay = MediaButtonReceiver.buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_PLAY)
    private val intentPause =
            MediaButtonReceiver.buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_PAUSE)
    private val intentStop = MediaButtonReceiver.buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_STOP)
    private val intentSkipToNext =
            MediaButtonReceiver.buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
    private val intentSkipToPrevious =
            MediaButtonReceiver.buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)

    private val actionPlay = NotificationCompat.Action(
            R.drawable.ic_play_arrow_24dp,
            service.getString(R.string.action_play),
            intentPlay
    )
    private val actionPause = NotificationCompat.Action(
            R.drawable.ic_pause_24dp,
            service.getString(R.string.action_pause),
            intentPause
    )
    private val actionSkipToNext = NotificationCompat.Action(
            R.drawable.ic_skip_next_24dp,
            service.getString(R.string.action_next),
            intentSkipToNext
    )
    private val actionSkipToPrevious = NotificationCompat.Action(
            R.drawable.ic_skip_previous_24dp,
            service.getString(R.string.action_previous),
            intentSkipToPrevious
    )

    fun updateNotification(
            metadata: MediaMetadataCompat,
            isPlaying: Boolean,
            sessionToken: MediaSessionCompat.Token?
    ) {
        notificationManager.notify(NOTIFICATION_ID, buildNotification(metadata.description, isPlaying, sessionToken))
    }

    fun updateNotification(
            mediaDescription: MediaDescriptionCompat,
            isPlaying: Boolean,
            sessionToken: MediaSessionCompat.Token?
    ) {
        notificationManager.notify(NOTIFICATION_ID, buildNotification(mediaDescription, isPlaying, sessionToken))
    }

    fun updateNotification(
            queueEntry: QueueEntry,
            isPlaying: Boolean,
            sessionToken: MediaSessionCompat.Token?
    ) {
        notificationManager.notify(NOTIFICATION_ID, buildNotification(queueEntry, isPlaying, sessionToken))
    }

    fun buildNotification(
            mediaDescription: MediaDescriptionCompat,
            isPlaying: Boolean,
            sessionToken: MediaSessionCompat.Token?
    ) = buildNotification(
            mediaDescription.title,
            mediaDescription.subtitle,
            mediaDescription.iconBitmap,
            isPlaying,
            sessionToken
    )

    fun buildNotification(
            queueEntry: QueueEntry,
            isPlaying: Boolean,
            sessionToken: MediaSessionCompat.Token?
    ): Notification {
        val mediaDescription = queueEntry.toMediaMetadata().description
        return buildNotification(
                mediaDescription.title,
                mediaDescription.subtitle,
                null,//queueEntry.image, TODO: Notification image
                isPlaying,
                sessionToken
        )
    }

    fun buildNotification(
            title: CharSequence?,
            subtitle: CharSequence?,
            iconBitmap: Bitmap?,
            isPlaying: Boolean,
            sessionToken: MediaSessionCompat.Token?
    ): Notification {
        createChannel()

        return NotificationCompat.Builder(service, CHANNEL_ID)
                .setStyle(
                        androidx.media.app.NotificationCompat.MediaStyle()
                                .setMediaSession(sessionToken)
                                .setShowActionsInCompactView(0, 1, 2)
                )
                .setSmallIcon(R.drawable.ic_music_note_24dp)
                .setContentIntent(createContentIntent())
                .setContentTitle(title)
                .setContentText(subtitle)
                .setLargeIcon(iconBitmap)
                .setDeleteIntent(intentStop)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(actionSkipToPrevious)
                .addAction(if (isPlaying) actionPause else actionPlay)
                .addAction(actionSkipToNext)
                .build()
    }

    private fun createChannel() {
        if (systemNotificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(CHANNEL_ID, "MediaSession", NotificationManager.IMPORTANCE_LOW)
                    .apply {
                        description = "Actively playing media"
                    }

            systemNotificationManager.createNotificationChannel(channel)
        }
    }

    private fun createContentIntent(): PendingIntent {
        val intent = Intent(service, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(service, REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    }
}