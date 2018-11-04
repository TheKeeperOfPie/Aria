package com.winsonchiu.aria.source.folders.util

import android.media.MediaMetadataRetriever
import androidx.annotation.WorkerThread
import com.winsonchiu.aria.framework.media.AudioMetadata
import com.winsonchiu.aria.framework.util.Failsafe
import com.winsonchiu.aria.source.folders.FolderScope
import java.io.File
import javax.inject.Inject

@FolderScope
class MetadataExtractor @Inject constructor() {

    companion object {
        val EMPTY = AudioMetadata()
    }

    private val mediaMetadataRetriever: ThreadLocal<MediaMetadataRetriever> = object : ThreadLocal<MediaMetadataRetriever>() {
        override fun initialValue(): MediaMetadataRetriever {
            return MediaMetadataRetriever()
        }
    }

    @WorkerThread
    fun extract(file: File): AudioMetadata {
        if (file.isDirectory) {
            return EMPTY
        }

        return Failsafe.withDefault(EMPTY) {
            mediaMetadataRetriever.get()!!.run {
                setDataSource(file.absolutePath)
                AudioMetadata(
                        cdTrackNumber = extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER),
                        album = extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                        artist = extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                        author = extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR),
                        composer = extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER),
                        date = extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE),
                        genre = extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE),
                        title = extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                        year = extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR),
                        duration = extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLongOrNull() ?: -1,
                        numTracks = extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS),
                        writer = extractMetadata(MediaMetadataRetriever.METADATA_KEY_WRITER),
                        mimeType = extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE),
                        albumArtist = extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST),
                        discNumber = extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER),
                        compilation = extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPILATION),
                        hasAudio = extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO),
                        hasVideo = extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO),
                        videoWidth = extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH),
                        videoHeight = extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT),
                        bitrate = extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE),
                        timedTextLanguages = extractMetadata(21), // Hidden METADATA_KEY_TIMED_TEXT_LANGUAGES
                        isDrm = extractMetadata(22), // Hidden METADATA_KEY_IS_DRM
                        location = extractMetadata(MediaMetadataRetriever.METADATA_KEY_LOCATION),
                        videoRotation = extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION),
                        captureFrameRate = extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)
                )
            }
        }
    }
}