package com.winsonchiu.aria.media

import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.winsonchiu.aria.application.AriaApplication
import com.winsonchiu.aria.dagger.ApplicationComponent
import com.winsonchiu.aria.media.util.toMediaMetadata
import javax.inject.Inject

class MediaService : LifecycleMediaBrowserService() {

    companion object {
        private val TAG = MediaService::class.java.simpleName
    }

    lateinit var mediaSession: MediaSessionCompat

    private lateinit var mediaNotificationManager: MediaNotificationManager

    private lateinit var player: MediaPlayer

    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {

        var state: PlaybackStateCompat? = null
        var metadata: MediaMetadataCompat? = null

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            this.state = state
            onChanged(state, metadata)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            this.metadata = metadata
            onChanged(state, metadata)
        }

        fun onChanged(state: PlaybackStateCompat?, metadata: MediaMetadataCompat?) {
            if (state != null && metadata != null) {
                mediaQueue.currentItem()?.let {
                    mediaNotificationManager.updateNotification(
                            it,
                            state.state == PlaybackStateCompat.STATE_PLAYING,
                            sessionToken
                    )
                }
            }
        }
    }

    @Inject
    lateinit var mediaDelegate: MediaDelegate

    @Inject
    lateinit var mediaQueue: MediaQueue

    private var isStarted = false

    private val playerListener = object : MediaPlayer.Listener {
        override fun onPlaybackStateChange(newState: PlaybackStateCompat) {

            when (newState.state) {
                PlaybackStateCompat.STATE_PLAYING -> mediaSession.isActive = true
                PlaybackStateCompat.STATE_STOPPED -> mediaSession.isActive = false
            }

            mediaSession.setPlaybackState(newState)

            mediaQueue.currentItem()?.let {
                val mediaMetadata = it.toMediaMetadata()
                mediaSession.setMetadata(mediaMetadata)

                val isPlaying = newState.state == PlaybackStateCompat.STATE_PLAYING
                val notification = mediaNotificationManager.buildNotification(
                        it,
                        isPlaying,
                        sessionToken
                )

                when (newState.state) {
                    PlaybackStateCompat.STATE_PLAYING -> {
                        if (!isStarted) {
                            startForegroundService(Intent(this@MediaService, MediaService::class.java))
                            isStarted = true
                        }

                        startForeground(MediaNotificationManager.NOTIFICATION_ID, notification)
                    }
                    PlaybackStateCompat.STATE_PAUSED -> {
                        stopForeground(false)
                        mediaNotificationManager.updateNotification(it, isPlaying, sessionToken)
                    }
                    PlaybackStateCompat.STATE_STOPPED -> {
                        stopForeground(true)
                        stopSelf()
                        isStarted = false
                    }
                    else -> {
                        mediaNotificationManager.updateNotification(it, isPlaying, sessionToken)
                    }
                }
            }
        }
    }

    private val musicList = mutableListOf<MediaBrowserCompat.MediaItem>()

    override fun onCreate() {
        super.onCreate()

        (applicationContext.getSystemService(AriaApplication.APPLICATION_COMPONENT) as ApplicationComponent).inject(this)
        mediaDelegate.playActions
                .map {
                    it.map { it.toMediaMetadata() }
                            .map {
                                MediaBrowserCompat.MediaItem(
                                        it.description,
                                        MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
                                )
                            }
                }
                .bindToLifecycle()
                .subscribe({
                    musicList.clear()
                    musicList.addAll(it)

                    notifyChildrenChanged("root")
                }, { it.printStackTrace() })

        super.onCreate()

        mediaNotificationManager = MediaNotificationManager(this)
        player = MediaPlayer(this)
        player.listener = playerListener

        mediaSession = MediaSessionCompat(this, "Aria_MusicService").apply {
            setCallback(player)
            setFlags(
                    MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                            MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS or
                            MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
        }

        sessionToken = mediaSession.sessionToken

        mediaSession.controller.registerCallback(mediaControllerCallback)
    }

    override fun onDestroy() {
        mediaSession.controller.unregisterCallback(mediaControllerCallback)
        mediaSession.setCallback(null)
        mediaSession.release()
        super.onDestroy()
    }

    override fun onGetRoot(
            clientPackageName: String,
            clientUid: Int,
            rootHints: Bundle?
    ): MediaBrowserServiceCompat.BrowserRoot? {
        return MediaBrowserServiceCompat.BrowserRoot("root", null)
    }

    override fun onLoadChildren(
            parentMediaId: String,
            result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>
    ) {
        result.sendResult(musicList)
    }
}