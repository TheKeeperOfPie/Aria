package com.winsonchiu.aria.media

import android.app.Application
import android.content.ComponentName
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.jakewharton.rxrelay2.BehaviorRelay
import com.winsonchiu.aria.framework.dagger.activity.ActivityLifecycleBoundComponent
import com.winsonchiu.aria.media.util.NullableMediaBrowserSubscriptionCallback
import java.util.Optional
import javax.inject.Inject

@MediaScreenScope
class MediaBrowserConnection @Inject constructor(
        application: Application
): ActivityLifecycleBoundComponent() {

    private val browserCallback = object : MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            super.onConnected()
            mediaController = MediaControllerCompat(application, mediaBrowser.sessionToken)
            mediaController.registerCallback(controllerCallback)

            controllerCallback.onMetadataChanged(mediaController.metadata)
            controllerCallback.onPlaybackStateChanged(mediaController.playbackState)

            mediaBrowser.subscribe(mediaBrowser.root, browserSubscriptionCallback)
        }
    }

    private val browserSubscriptionCallback = object : NullableMediaBrowserSubscriptionCallback() {

        override fun runChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>,
                options: Bundle?
        ) {
            super.runChildrenLoaded(parentId, children, options)

            for (mediaItem in children) {
                mediaController.addQueueItem(mediaItem.description)
            }

            mediaController.transportControls.prepare()
        }
    }

    val mediaBrowser: MediaBrowserCompat = MediaBrowserCompat(
            application,
            ComponentName(application, MediaService::class.java),
            browserCallback,
            null
    )

    lateinit var mediaController: MediaControllerCompat

    val metadataChanges = BehaviorRelay.create<Optional<MediaMetadataCompat>>()
    val playbackStateChanges = BehaviorRelay.create<Optional<PlaybackStateCompat>>()

    private val controllerCallback = object : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            metadataChanges.accept(Optional.ofNullable(metadata))
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            playbackStateChanges.accept(Optional.ofNullable(state))
        }

        override fun onSessionDestroyed() {
            playbackStateChanges.accept(null)
        }
    }

    override fun onFirstInitialize() {
        super.onFirstInitialize()
        mediaBrowser.connect()
    }

    override fun onFinalDestroy() {
        super.onFinalDestroy()

        if (::mediaController.isInitialized) {
            mediaController.unregisterCallback(controllerCallback)
        }

        mediaBrowser.disconnect()
    }
}