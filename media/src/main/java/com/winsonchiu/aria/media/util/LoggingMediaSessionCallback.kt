package com.winsonchiu.aria.media.util

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.RatingCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.annotation.CallSuper

abstract class LoggingMediaSessionCallback : MediaSessionCompat.Callback() {

    @CallSuper
    override fun onCommand(command: String?, extras: Bundle?, cb: ResultReceiver?) {
        Log.d(TAG, "onCommand() called with: command = [$command], extras = [$extras], cb = [$cb]")
        super.onCommand(command, extras, cb)
    }

    @CallSuper
    override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
        Log.d(TAG, "onMediaButtonEvent() called with: mediaButtonEvent = [$mediaButtonEvent]")
        return super.onMediaButtonEvent(mediaButtonEvent)
    }

    @CallSuper
    override fun onPrepare() {
        Log.d(TAG, "onPrepare() called with")
        super.onPrepare()
    }

    @CallSuper
    override fun onPrepareFromMediaId(mediaId: String?, extras: Bundle?) {
        Log.d(TAG, "onPrepareFromMediaId() called with: mediaId = [$mediaId], extras = [$extras]")
        super.onPrepareFromMediaId(mediaId, extras)
    }

    @CallSuper
    override fun onPrepareFromSearch(query: String?, extras: Bundle?) {
        Log.d(TAG, "onPrepareFromSearch() called with: query = [$query], extras = [$extras]")
        super.onPrepareFromSearch(query, extras)
    }

    @CallSuper
    override fun onPrepareFromUri(uri: Uri?, extras: Bundle?) {
        Log.d(TAG, "onPrepareFromUri() called with: uri = [$uri], extras = [$extras]")
        super.onPrepareFromUri(uri, extras)
    }

    @CallSuper
    override fun onPlay() {
        Log.d(TAG, "onPlay() called with")
        super.onPlay()
    }

    @CallSuper
    override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
        Log.d(TAG, "onPlayFromMediaId() called with: mediaId = [$mediaId], extras = [$extras]")
        super.onPlayFromMediaId(mediaId, extras)
    }

    @CallSuper
    override fun onPlayFromSearch(query: String?, extras: Bundle?) {
        Log.d(TAG, "onPlayFromSearch() called with: query = [$query], extras = [$extras]")
        super.onPlayFromSearch(query, extras)
    }

    @CallSuper
    override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
        Log.d(TAG, "onPlayFromUri() called with: uri = [$uri], extras = [$extras]")
        super.onPlayFromUri(uri, extras)
    }

    @CallSuper
    override fun onSkipToQueueItem(id: Long) {
        Log.d(TAG, "onSkipToQueueItem() called with: id = [$id]")
        super.onSkipToQueueItem(id)
    }

    @CallSuper
    override fun onPause() {
        Log.d(TAG, "onPause() called with")
        super.onPause()
    }

    @CallSuper
    override fun onSkipToNext() {
        Log.d(TAG, "onSkipToNext() called with")
        super.onSkipToNext()
    }

    @CallSuper
    override fun onSkipToPrevious() {
        Log.d(TAG, "onSkipToPrevious() called with")
        super.onSkipToPrevious()
    }

    @CallSuper
    override fun onFastForward() {
        Log.d(TAG, "onFastForward() called with")
        super.onFastForward()
    }

    @CallSuper
    override fun onRewind() {
        Log.d(TAG, "onRewind() called with")
        super.onRewind()
    }

    @CallSuper
    override fun onStop() {
        Log.d(TAG, "onStop() called with")
        super.onStop()
    }

    @CallSuper
    override fun onSeekTo(pos: Long) {
        Log.d(TAG, "onSeekTo() called with: pos = [$pos]")
        super.onSeekTo(pos)
    }

    @CallSuper
    override fun onSetRating(rating: RatingCompat?) {
        Log.d(TAG, "onSetRating() called with: rating = [$rating]")
        super.onSetRating(rating)
    }

    @CallSuper
    override fun onSetRating(rating: RatingCompat?, extras: Bundle?) {
        Log.d(TAG, "onSetRating() called with: rating = [$rating], extras = [$extras]")
        super.onSetRating(rating, extras)
    }

    @CallSuper
    override fun onSetCaptioningEnabled(enabled: Boolean) {
        Log.d(TAG, "onSetCaptioningEnabled() called with: enabled = [$enabled]")
        super.onSetCaptioningEnabled(enabled)
    }

    @CallSuper
    override fun onSetRepeatMode(repeatMode: Int) {
        Log.d(TAG, "onSetRepeatMode() called with: repeatMode = [$repeatMode]")
        super.onSetRepeatMode(repeatMode)
    }

    @CallSuper
    override fun onSetShuffleMode(shuffleMode: Int) {
        Log.d(TAG, "onSetShuffleMode() called with: shuffleMode = [$shuffleMode]")
        super.onSetShuffleMode(shuffleMode)
    }

    @CallSuper
    override fun onCustomAction(action: String?, extras: Bundle?) {
        Log.d(TAG, "onCustomAction() called with: action = [$action], extras = [$extras]")
        super.onCustomAction(action, extras)
    }

    @CallSuper
    override fun onAddQueueItem(description: MediaDescriptionCompat) {
        Log.d(TAG, "onAddQueueItem() called with: description = [$description]")
        super.onAddQueueItem(description)
    }

    @CallSuper
    override fun onAddQueueItem(description: MediaDescriptionCompat, index: Int) {
        Log.d(TAG, "onAddQueueItem() called with: description = [$description], index = [$index]")
        super.onAddQueueItem(description, index)
    }

    @CallSuper
    override fun onRemoveQueueItem(description: MediaDescriptionCompat) {
        Log.d(TAG, "onRemoveQueueItem() called with: description = [$description]")
        super.onRemoveQueueItem(description)
    }

    companion object {

        val TAG = LoggingMediaSessionCallback::class.java.canonicalName
    }
}
