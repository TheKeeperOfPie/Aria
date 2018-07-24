package com.winsonchiu.aria.music

import android.media.MediaMetadataRetriever
import android.os.Parcelable
import android.support.annotation.WorkerThread
import com.winsonchiu.aria.framework.dagger.ApplicationScope
import com.winsonchiu.aria.framework.util.Failsafe
import kotlinx.android.parcel.Parcelize
import java.io.File
import javax.inject.Inject

@ApplicationScope
class MetadataExtractor @Inject constructor() {

    companion object {
        val EMPTY = Metadata()
    }

    private val mediaMetadataRetriever = object : ThreadLocal<MediaMetadataRetriever>() {
        override fun initialValue(): MediaMetadataRetriever {
            return MediaMetadataRetriever()
        }
    }

    @WorkerThread
    fun extract(file: File): Metadata {
        if (file.isDirectory) {
            return EMPTY
        }

        return Failsafe.withDefault(EMPTY) {
            mediaMetadataRetriever.get().run {
                setDataSource(file.absolutePath)
                Metadata(
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER),
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR),
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER),
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE),
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE),
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR),
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLongOrNull() ?: -1,
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS),
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_WRITER),
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE),
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST),
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER),
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPILATION),
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO),
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO),
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH),
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT),
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE),
                        extractMetadata(21), // Hidden METADATA_KEY_TIMED_TEXT_LANGUAGES
                        extractMetadata(22), // Hidden METADATA_KEY_IS_DRM
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_LOCATION),
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION),
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)
                )
            }
        }
    }

    @Parcelize
    data class Metadata(
            val cdTrackNumber: String? = null,
            val album: String? = null,
            val artist: String? = null,
            val author: String? = null,
            val composer: String? = null,
            val date: String? = null,
            val genre: String? = null,
            val title: String? = null,
            val year: String? = null,
            val duration: Long = -1,
            val numTracks: String? = null,
            val writer: String? = null,
            val mimeType: String? = null,
            val albumArtist: String? = null,
            val discNumber: String? = null,
            val compilation: String? = null,
            val hasAudio: String? = null,
            val hasVideo: String? = null,
            val videoWidth: String? = null,
            val videoHeight: String? = null,
            val bitrate: String? = null,
            val timedTextLanguages: String? = null,
            val isDrm: String? = null,
            val location: String? = null,
            val videoRotation: String? = null,
            val captureFrameRate: String? = null
    ) : Parcelable
}

fun MetadataExtractor.Metadata?.artistDisplayValue(): String? {
    return (this ?: return null).artist ?: albumArtist ?: author
}