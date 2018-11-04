package com.winsonchiu.aria.source.artists

import android.content.Context
import android.provider.MediaStore
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import com.winsonchiu.aria.framework.util.dpToPx
import com.winsonchiu.aria.source.artists.artist.media.ArtistMedia

internal object ArtistsUtils {

    private val MEDIA_PROJECTION = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM_KEY,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ARTIST_KEY,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DEFAULT_SORT_ORDER,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.TITLE_KEY,
            MediaStore.Audio.Media.TRACK
    )

    fun spanCount(context: Context): Int {
        return (context.resources.displayMetrics.widthPixels / 120f.dpToPx(context)).toInt().coerceAtLeast(3)
    }

    fun readMedia(
            application: Context,
            artistId: ArtistId
    ): List<ArtistMedia> {
        val media = mutableListOf<ArtistMedia>()

        application.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MEDIA_PROJECTION,
                "${MediaStore.Audio.Media.ARTIST_ID}=?",
                arrayOf(artistId.value),
                null
        ).use {
            if (it.moveToFirst()) {
                val idIndex = it.getColumnIndex(MediaStore.Audio.Media._ID)
                val albumIndex = it.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val albumIdIndex = it.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val albumKeyIndex = it.getColumnIndex(MediaStore.Audio.Media.ALBUM_KEY)
                val artistIndex = it.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val artistKeyIndex = it.getColumnIndex(MediaStore.Audio.Media.ARTIST_KEY)
                val artistIdIndex = it.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)
                val dataIndex = it.getColumnIndex(MediaStore.Audio.Media.DATA)
                val defaultSortOrderIndex = it
                        .getColumnIndex(MediaStore.Audio.Media.DEFAULT_SORT_ORDER)
                val displayNameIndex = it.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                val durationIndex = it.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val titleIndex = it.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val titleKeyIndex = it.getColumnIndex(MediaStore.Audio.Media.TITLE_KEY)
                val trackIndex = it.getColumnIndex(MediaStore.Audio.Media.TRACK)

                do {
                    try {
                        media += ArtistMedia(
                                id = it.getString(idIndex),
                                album = it.getStringOrNull(albumIndex),
                                albumId = it.getStringOrNull(albumIdIndex),
                                albumKey = it.getStringOrNull(albumKeyIndex),
                                artist = it.getStringOrNull(artistIndex),
                                artistKey = it.getStringOrNull(artistKeyIndex),
                                artistId = it.getStringOrNull(artistIdIndex),
                                data = it.getString(dataIndex),
                                defaultSortOrder = it.getStringOrNull(defaultSortOrderIndex),
                                displayName = it.getStringOrNull(displayNameIndex),
                                duration = it.getLongOrNull(durationIndex),
                                title = it.getStringOrNull(titleIndex),
                                titleKey = it.getStringOrNull(titleKeyIndex),
                                track = it.getLongOrNull(trackIndex)
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } while (it.moveToNext())
            }
        }

        return media
    }
}