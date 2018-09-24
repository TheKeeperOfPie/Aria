package com.winsonchiu.aria.media.util

import android.support.v4.media.MediaMetadataCompat
import com.winsonchiu.aria.queue.QueueEntry
import com.winsonchiu.aria.source.folder.inner.FolderController
import com.winsonchiu.aria.source.folder.util.artistDisplayValue

fun FolderController.FileMetadata.toMediaMetadata() = MediaMetadataCompat.Builder()
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, file.absolutePath)
        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, metadata?.album)
        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, metadata.artistDisplayValue())
        .putString(MediaMetadataCompat.METADATA_KEY_GENRE, metadata?.genre)
        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, metadata?.title)
//        .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, image?.bitmap)
        .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, null)
        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, metadata?.duration ?: -1L)
        .build()

// TODO: Album art
fun QueueEntry.toMediaMetadata() = MediaMetadataCompat.Builder()
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, content.toString())
        .putText(MediaMetadataCompat.METADATA_KEY_ALBUM, metadata.album)
        .putText(MediaMetadataCompat.METADATA_KEY_ARTIST, metadata.artist)
        .putText(MediaMetadataCompat.METADATA_KEY_GENRE, metadata.genre)
        .putText(MediaMetadataCompat.METADATA_KEY_TITLE, metadata.title)
//        .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, image)
        .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, null)
        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, metadata.duration)
        .build()

//fun MediaQueue.QueueEntry.toMediaMetadata() = MediaMetadataCompat.Builder()
//        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, file.absolutePath)
//        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, metadata?.album)
//        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, metadata.artistDisplayValue())
//        .putString(MediaMetadataCompat.METADATA_KEY_GENRE, metadata?.genre)
//        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, metadata?.title ?: file.nameWithoutExtension)
////        .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, image)
//        .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, null)
//        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, metadata?.duration ?: -1L)
//        .build()