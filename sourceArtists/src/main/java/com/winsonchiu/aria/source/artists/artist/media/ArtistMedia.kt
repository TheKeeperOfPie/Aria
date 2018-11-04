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
        val displayName: String?,
        val duration: Long?,
        val title: String?,
        val titleKey: String?,
        val track: Long?
): Parcelable {

    @IgnoredOnParcel
    val image by lazy {
        ArtworkRequestHandler.albumOrArtistUri(albumId, artistId)
    }

    @IgnoredOnParcel
    val displayTitle = title ?: displayName ?: titleKey

    @IgnoredOnParcel
    var description = album ?: albumKey

    fun toQueueEntry(context: Context) = QueueEntry(
            content = Uri.parse(data),
            image = ArtworkRequestHandler.albumOrArtistUri(albumId, artistId),
            metadata = QueueEntry.Metadata(
                    title = displayTitle,
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