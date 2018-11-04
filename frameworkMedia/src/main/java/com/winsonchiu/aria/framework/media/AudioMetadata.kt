package com.winsonchiu.aria.framework.media

import android.content.Context
import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

interface AudioMetadataInterface {
    val cdTrackNumber: String?
    val album: String?
    val artist: String?
    val author: String?
    val composer: String?
    val date: String?
    val genre: String?
    val title: String?
    val year: String?
    val duration: Long
    val numTracks: String?
    val writer: String?
    val mimeType: String?
    val albumArtist: String?
    val discNumber: String?
    val compilation: String?
    val hasAudio: String?
    val hasVideo: String?
    val videoWidth: String?
    val videoHeight: String?
    val bitrate: String?
    val timedTextLanguages: String?
    val isDrm: String?
    val location: String?
    val videoRotation: String?
    val captureFrameRate: String?

    fun getDisplayTitle(
            context: Context
    ): CharSequence?

    fun getDescription(
            context: Context,
            showArtist: Boolean = true,
            showAlbum: Boolean = true
    ): CharSequence?
}

@Parcelize
data class AudioMetadata(
        override val cdTrackNumber: String? = null,
        override val album: String? = null,
        override val artist: String? = null,
        override val author: String? = null,
        override val composer: String? = null,
        override val date: String? = null,
        override val genre: String? = null,
        override val title: String? = null,
        override val year: String? = null,
        override val duration: Long = -1,
        override val numTracks: String? = null,
        override val writer: String? = null,
        override val mimeType: String? = null,
        override val albumArtist: String? = null,
        override val discNumber: String? = null,
        override val compilation: String? = null,
        override val hasAudio: String? = null,
        override val hasVideo: String? = null,
        override val videoWidth: String? = null,
        override val videoHeight: String? = null,
        override val bitrate: String? = null,
        override val timedTextLanguages: String? = null,
        override val isDrm: String? = null,
        override val location: String? = null,
        override val videoRotation: String? = null,
        override val captureFrameRate: String? = null
) : AudioMetadataInterface, Parcelable {

    companion object {
        val EMPTY = AudioMetadata()
    }

    @IgnoredOnParcel
    val artistDisplayValue by lazy {
        artist.takeIf { it.isShowable() }
                ?: albumArtist.takeIf { it.isShowable() }
                ?: author.takeIf { it.isShowable() }
    }

    @IgnoredOnParcel
    private var description: CharSequence? = null
    @IgnoredOnParcel
    private var descriptionSet = false

    override fun getDisplayTitle(context: Context): CharSequence? {
        return title
    }

    override fun getDescription(
            context: Context,
            showArtist: Boolean,
            showAlbum: Boolean
    ): CharSequence? {
        if (!descriptionSet) {
            descriptionSet = true
            description = AudioMetadataUtils.getDescription(
                    context = context,
                    artist = artistDisplayValue.takeIf { showArtist },
                    album = album.takeIf { showAlbum }
            )
        }

        return description
    }
}