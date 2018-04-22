package com.winsonchiu.aria.media

import android.content.Intent
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.support.v4.media.AudioAttributesCompat.CONTENT_TYPE_MUSIC
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.C.USAGE_MEDIA
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.winsonchiu.aria.R
import com.winsonchiu.aria.application.AriaApplication
import com.winsonchiu.aria.dagger.ApplicationComponent
import com.winsonchiu.aria.media.util.LoggingMediaSessionCallback
import com.winsonchiu.aria.media.util.toMediaMetadata
import com.winsonchiu.aria.util.arch.LoggingLifecycleObserver
import javax.inject.Inject

class MediaPlayer(
        val service: MediaService,
        private val mediaNotificationManager: MediaNotificationManager
) : LoggingLifecycleObserver, LoggingMediaSessionCallback() {

    @Inject
    lateinit var mediaQueue: MediaQueue

    private val player = ExoPlayerFactory.newSimpleInstance(service, DefaultTrackSelector())

    private val extractorFactory = ExtractorMediaSource.Factory(
            DefaultDataSourceFactory(
                    service,
                    Util.getUserAgent(service, service.getString(R.string.app_name))
            )
    )

    private var isStarted = false

    init {
        service.lifecycle.addObserver(this)
        (service.application.getSystemService(AriaApplication.APPLICATION_COMPONENT) as ApplicationComponent).inject(
                this
        )

        mediaQueue.queueUpdates
                .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(service)))
                .subscribe { onPlay() }

        player.addListener(object : Player.DefaultEventListener() {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)

                service.mediaSession.setPlaybackState(PlaybackStateCompat.Builder()
                        .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                        .setState(PlaybackStateCompat.STATE_PLAYING, player.currentPosition, player.playbackParameters.speed)
                        .build())

                mediaQueue.currentItem()?.toMediaMetadata()?.let {
                    val isPlaying = getState() == PlaybackStateCompat.STATE_PLAYING
                    val notification = mediaNotificationManager.buildNotification(
                            it.description,
                            isPlaying,
                            service.sessionToken
                    )

                    when (getState()) {
                        PlaybackStateCompat.STATE_PLAYING -> {
                            if (!isStarted) {
                                ContextCompat.startForegroundService(
                                        service,
                                        Intent(service, MediaService::class.java)
                                )
                                isStarted = true
                            }

                            service.startForeground(
                                    MediaNotificationManager.NOTIFICATION_ID,
                                    notification
                            )
                        }
                        PlaybackStateCompat.STATE_PAUSED -> {
                            service.stopForeground(false)
                            mediaNotificationManager.updateNotification(it.description, isPlaying, service.sessionToken)
                        }
                        PlaybackStateCompat.STATE_STOPPED -> {
                            service.stopForeground(true)
                            service.stopSelf()
                            isStarted = false
                        }
                    }
                }

                if (playbackState == Player.STATE_ENDED) {
                    onSkipToNext()
                }
            }
        })
    }

    private fun play(queueItem: MediaQueue.QueueItem) {
        player.audioAttributes = AudioAttributes.Builder()
                .setContentType(CONTENT_TYPE_MUSIC)
                .setUsage(USAGE_MEDIA)
                .build()

        val source = extractorFactory.createMediaSource(Uri.fromFile(queueItem.file))
        player.prepare(source)
        player.playWhenReady = true
    }

    override fun onPlay() {
        super.onPlay()
        mediaQueue.currentItem()?.run(::play)
    }

    override fun onPause() {
        super<LoggingMediaSessionCallback>.onPause()
        player.playWhenReady = false
    }

    override fun onSkipToNext() {
        super.onSkipToNext()
        mediaQueue.next()?.let(::play)
    }

    override fun onSkipToPrevious() {
        super.onSkipToPrevious()
        mediaQueue.previous()?.let(::play)
    }

    override fun onStop() {
        super<LoggingMediaSessionCallback>.onStop()
        player.stop()
    }

    private fun getState() = when (player.playbackState) {
        Player.STATE_IDLE -> PlaybackStateCompat.STATE_PAUSED
        Player.STATE_BUFFERING -> PlaybackStateCompat.STATE_BUFFERING
        Player.STATE_READY -> if (player.playWhenReady)
            PlaybackStateCompat.STATE_PLAYING
        else
            PlaybackStateCompat.STATE_PAUSED
        Player.STATE_ENDED -> PlaybackStateCompat.STATE_PAUSED
        else -> PlaybackStateCompat.STATE_NONE
    }
}