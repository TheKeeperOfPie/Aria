package com.winsonchiu.aria.media.util

import android.support.v4.media.MediaMetadataCompat
import com.winsonchiu.aria.folders.folder.FolderController
import com.winsonchiu.aria.media.MediaQueue
import com.winsonchiu.aria.music.artistDisplayValue

fun FolderController.FileMetadata.toMediaMetadata(): MediaMetadataCompat = MediaMetadataCompat.Builder()
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, file.absolutePath)
        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, metadata?.album)
        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, metadata.artistDisplayValue())
        .putString(MediaMetadataCompat.METADATA_KEY_GENRE, metadata?.genre)
        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, metadata?.title)
        .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, image?.bitmap)
        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, metadata?.duration ?: -1L)
        .build()

// TODO: Album art
fun MediaQueue.QueueItem.toMediaMetadata(): MediaMetadataCompat = MediaMetadataCompat.Builder()
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, file.absolutePath)
        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, metadata?.album)
        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, metadata.artistDisplayValue())
        .putString(MediaMetadataCompat.METADATA_KEY_GENRE, metadata?.genre)
        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, metadata?.title)
        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, metadata?.duration ?: -1L)
        .build()