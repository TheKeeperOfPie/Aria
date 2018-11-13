package com.winsonchiu.aria.media

import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_MUSIC
import android.media.AudioAttributes.USAGE_MEDIA
import android.media.MediaPlayer
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.media.session.PlaybackStateCompat
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import com.winsonchiu.aria.framework.dagger.activity.DaggerConstants
import com.winsonchiu.aria.framework.util.arch.LoggingLifecycleObserver
import com.winsonchiu.aria.media.transport.MediaAction
import com.winsonchiu.aria.media.transport.MediaTransport
import com.winsonchiu.aria.media.util.LoggingMediaSessionCallback
import com.winsonchiu.aria.queue.MediaQueue
import com.winsonchiu.aria.queue.QueueEntry
import java.util.Optional
import javax.inject.Inject

// TODO: Rename?
class MediaPlayer(
        private val service: MediaService
) : LoggingLifecycleObserver, LoggingMediaSessionCallback() {

    companion object {
        val AUDIO_SESSION_ID = "${MediaPlayer::class.java.canonicalName}.audioSessionId"
        val DURATION = "${MediaPlayer::class.java.canonicalName}.duration"
    }

    @Inject
    lateinit var mediaQueue: MediaQueue

    var listener: Listener? = null

    private val player = MediaPlayer()

    private val playbackStateBuilder = PlaybackStateCompat.Builder()

    private var lastEntry: QueueEntry? = null

    private var seekWhileNotPlaying = 0L

    @PlaybackStateCompat.State
    private var currentState = PlaybackStateCompat.STATE_STOPPED

    private var initializedPlayer = false

    init {
        service.lifecycle.addObserver(this)
        (service.application.getSystemService(DaggerConstants.APPLICATION_COMPONENT) as MediaInjector).inject(
                this
        )

        MediaTransport.mediaActions
                .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(service)))
                .subscribe {
                    when (it) {
                        MediaAction.Play -> {
                            if (!player.isPlaying) {
                                onPlay()
                            }
                            Unit
                        }
                        MediaAction.Pause -> {
                            if (player.isPlaying) {
                                onPause()
                            }
                            Unit
                        }
                        MediaAction.PlayPause -> {
                            if (player.isPlaying) {
                                onPause()
                            } else {
                                onPlay()
                            }
                        }
                        MediaAction.SkipPrevious -> onSkipToPrevious()
                        MediaAction.SkipNext -> onSkipToNext()
                        is MediaAction.Seek -> {
                            val duration = player.duration.toLong()
                            onSeekTo((it.progress * duration).toLong().coerceIn(0, duration))
                        }
                    }.run{  }
                }

        mediaQueue.queueUpdates
                .map { Optional.ofNullable(it.currentEntry) }
                .distinctUntilChanged()
                .autoDisposable(AndroidLifecycleScopeProvider.from(service))
                .subscribe {
                    if (!it.isPresent) {
                        if (player.isPlaying) {
                            onStop()
                        }
                    } else if (player.isPlaying) {
                        onPlay()
                    } else {
                        setEntry(it.get())
                    }
                }

        player.setOnCompletionListener { onSkipToNext() }
    }

    private fun setEntry(queueEntry: QueueEntry) {
        if (lastEntry == queueEntry) {
            return
        }

        lastEntry = queueEntry

        player.reset()

        try {
            player.setDataSource(service, queueEntry.content)
            player.setAudioAttributes(
                    AudioAttributes.Builder()
                            .setContentType(CONTENT_TYPE_MUSIC)
                            .setUsage(USAGE_MEDIA)
                            .build()
            )

            player.prepare()

            initializedPlayer = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun play(queueEntry: QueueEntry) {
        if (lastEntry == queueEntry) {
            player.start()
            return
        }

        setEntry(queueEntry)

        player.start()
    }

    override fun onPlay() {
        super.onPlay()
        mediaQueue.currentItem()?.run(::play) ?: return
        onNewState(PlaybackStateCompat.STATE_PLAYING)
    }

    override fun onPause() {
        super<LoggingMediaSessionCallback>.onPause()
        player.pause()
        onNewState(PlaybackStateCompat.STATE_PAUSED)
    }

    override fun onSkipToNext() {
        super.onSkipToNext()
        mediaQueue.next()?.let(::play) ?: return
        onNewState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT)
        onNewState(PlaybackStateCompat.STATE_PLAYING)
    }

    override fun onSkipToPrevious() {
        super.onSkipToPrevious()
        mediaQueue.previous()?.let(::play) ?: return
        onNewState(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS)
        onNewState(PlaybackStateCompat.STATE_PLAYING)
    }

    override fun onStop() {
        super<LoggingMediaSessionCallback>.onStop()
        player.stop()
        onNewState(PlaybackStateCompat.STATE_STOPPED)
    }

    override fun onSeekTo(pos: Long) {
        super.onSeekTo(pos)

        if (!initializedPlayer) {
            return
        }

        if (!player.isPlaying) {
            seekWhileNotPlaying = pos
        }

        player.seekTo(pos, MediaPlayer.SEEK_CLOSEST)
        onNewState(currentState)
    }

    private fun onNewState(@PlaybackStateCompat.State newState: Int) {
        currentState = newState

        // Work around for MediaPlayer.getCurrentPosition() when it changes while not playing.
        val reportPosition: Long
        if (seekWhileNotPlaying >= 0L) {
            reportPosition = seekWhileNotPlaying

            if (currentState == PlaybackStateCompat.STATE_PLAYING) {
                seekWhileNotPlaying = -1L
            }
        } else {
            reportPosition = player.currentPosition.toLong()
        }

        playbackStateBuilder.setActions(
                PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_PAUSE
                        or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        )

        playbackStateBuilder.setState(
                currentState,
                reportPosition,
                player.playbackParams.speed,
                SystemClock.elapsedRealtime()
        )
                .setExtras(Bundle().apply {
                    putInt(AUDIO_SESSION_ID, player.audioSessionId)
                    putInt(DURATION, player.duration)
                })

        listener?.onPlaybackStateChange(playbackStateBuilder.build())
    }

    interface Listener {
        fun onPlaybackStateChange(newState: PlaybackStateCompat)
    }
}