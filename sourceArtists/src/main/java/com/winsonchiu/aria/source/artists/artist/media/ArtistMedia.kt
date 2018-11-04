package com.winsonchiu.aria.source.artists.artist.media

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import com.winsonchiu.aria.artwork.ArtworkRequestHandler
import com.winsonchiu.aria.framework.media.AudioMetadataUtils
import com.winsonchiu.aria.queue.QueueEntry
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ArtistMedia(
        val id: String,
        val album: String?,
        val albumId: String?,
        val albumKey: String?,
        val artist: String?,
        val artistKey: String?,
        val artistId: String?,
        val data: String,
        val defaultSortOrder: String?,
        private val _displayName: String?,
        val duration: Long?,
        private val _title: String?,
        val titleKey: String?,
        val track: Long?
): Parcelable {

    @IgnoredOnParcel
    val image by lazy {
        ArtworkRequestHandler.albumOrArtistUri(albumId, artistId)
    }

    @IgnoredOnParcel
    val title = _displayName ?: _title ?: titleKey

    @IgnoredOnParcel
    var description = album ?: albumKey

    fun toQueueEntry(context: Context) = QueueEntry(
            content = Uri.parse(data),
            image = ArtworkRequestHandler.albumOrArtistUri(albumId, artistId),
            metadata = QueueEntry.Metadata(
                    // Prefer title without media prefix
                    title = _title ?: titleKey ?: _displayName,
                    description = AudioMetadataUtils.getDescription(
                            context = context,
                            artist = artist ?: artistKey,
                            album = album ?: albumKey
                    ),
                    album = album ?: albumKey,
                    artist = artist ?: artistKey,
                    genre = null, // TODO: Parse genre?
                    duration = duration ?: -1L
            )
    )
}