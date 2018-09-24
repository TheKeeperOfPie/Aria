package com.winsonchiu.aria.media

import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_MUSIC
import android.media.AudioAttributes.USAGE_MEDIA
import android.media.MediaPlayer
import android.os.SystemClock
import android.support.v4.media.session.PlaybackStateCompat
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.winsonchiu.aria.framework.application.AriaApplication
import com.winsonchiu.aria.framework.dagger.ApplicationComponent
import com.winsonchiu.aria.framework.util.arch.LoggingLifecycleObserver
import com.winsonchiu.aria.media.util.LoggingMediaSessionCallback
import com.winsonchiu.aria.queue.MediaQueue
import com.winsonchiu.aria.queue.QueueEntry
import io.reactivex.Maybe
import javax.inject.Inject

class MediaPlayer(
        private val service: MediaService
) : LoggingLifecycleObserver, LoggingMediaSessionCallback() {

    @Inject
    lateinit var mediaQueue: MediaQueue

    var listener: Listener? = null

    private val player = MediaPlayer()

    private val playbackStateBuilder = PlaybackStateCompat.Builder()

    private var lastEntry: QueueEntry? = null

    private var seekWhileNotPlaying = 0L

    @PlaybackStateCompat.State
    private var currentState = PlaybackStateCompat.STATE_STOPPED

    init {
        service.lifecycle.addObserver(this)
        (service.application.getSystemService(AriaApplication.APPLICATION_COMPONENT) as ApplicationComponent).inject(
                this
        )

        mediaQueue.playPauseActions
                .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(service)))
                .subscribe {
                    if (player.isPlaying) {
                        onPause()
                    } else {
                        onPlay()
                    }
                }

        mediaQueue.queueUpdates
                .flatMapMaybe { Maybe.fromCallable { it.currentEntry } }
                .distinctUntilChanged()
                .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(service)))
                .subscribe {
                    if (player.isPlaying) {
                        onPlay()
                    }
                }

        player.setOnCompletionListener { onSkipToNext() }
    }

    private fun play(queueEntry: QueueEntry) {
        if (lastEntry == queueEntry) {
            player.start()
            return
        }

        player.reset()
        player.setDataSource(service, queueEntry.content)
        player.setAudioAttributes(
                AudioAttributes.Builder()
                        .setContentType(CONTENT_TYPE_MUSIC)
                        .setUsage(USAGE_MEDIA)
                        .build()
        )

        player.prepare()
        player.start()
        lastEntry = queueEntry
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

    fun seekTo(position: Long) {
        if (!player.isPlaying) {
            seekWhileNotPlaying = position
        }

        player.seekTo(position, MediaPlayer.SEEK_CLOSEST)
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

        listener?.onPlaybackStateChange(playbackStateBuilder.build())
    }

    interface Listener {
        fun onPlaybackStateChange(newState: PlaybackStateCompat)
    }
}