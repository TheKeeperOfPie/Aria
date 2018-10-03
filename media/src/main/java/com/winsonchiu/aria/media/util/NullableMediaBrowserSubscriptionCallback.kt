package com.winsonchiu.aria.media.util

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import androidx.annotation.CallSuper

abstract class NullableMediaBrowserSubscriptionCallback : MediaBrowserCompat.SubscriptionCallback() {

    @CallSuper
    open fun runChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>,
            options: Bundle?
    ) {

    }

    @CallSuper
    open fun runError(parentId: String, options: Bundle?) {

    }

    final override fun onChildrenLoaded(parentId: String, children: MutableList<MediaBrowserCompat.MediaItem>) {
        super.onChildrenLoaded(parentId, children)
        runChildrenLoaded(parentId, children, null)
    }

    final override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>,
            options: Bundle
    ) {
        super.onChildrenLoaded(parentId, children, options)
        runChildrenLoaded(parentId, children, options)
    }

    final override fun onError(parentId: String) {
        super.onError(parentId)
        runError(parentId, null)
    }

    final override fun onError(parentId: String, options: Bundle) {
        super.onError(parentId, options)
        runError(parentId, options)
    }
}