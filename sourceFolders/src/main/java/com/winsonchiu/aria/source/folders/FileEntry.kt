package com.winsonchiu.aria.source.folders

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import com.winsonchiu.aria.artwork.ArtworkRequestHandler
import com.winsonchiu.aria.framework.media.AudioMetadata
import com.winsonchiu.aria.framework.media.AudioMetadataInterface
import com.winsonchiu.aria.queue.QueueEntry
import com.winsonchiu.aria.source.folders.util.FileUtils
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.io.File

sealed class FileEntry(
        open val file: File
) : Parcelable {

    abstract val image: Uri?

    abstract fun getDisplayTitle(context: Context): CharSequence?

    abstract fun getDescription(context: Context): CharSequence?

    abstract override fun equals(other: Any?): Boolean

    abstract override fun hashCode(): Int

    @Parcelize
    data class Folder(
            override val file: File
    ) : FileEntry(file) {

        @IgnoredOnParcel
        override val image by lazy {
            ArtworkRequestHandler.musicFileUri(file)
        }

        @IgnoredOnParcel
        private val displayTitle by lazy {
            FileUtils.getFileDisplayTitle(
                    FileUtils.getFileSortKey(file)?.substringBeforeLast(
                            "."
                    )
            )
        }

        override fun getDisplayTitle(context: Context) = displayTitle

        override fun getDescription(context: Context): CharSequence? = null
    }

    @Parcelize
    data class Playlist(
            override val file: File
    ) : FileEntry(file) {

        @IgnoredOnParcel
        override val image: Uri? = null

        @IgnoredOnParcel
        private val displayTitle by lazy {
            FileUtils.getFileDisplayTitle(
                    FileUtils.getFileSortKey(file)?.substringBeforeLast(
                            "."
                    )
            )
        }

        override fun getDisplayTitle(context: Context) = displayTitle

        override fun getDescription(context: Context): CharSequence? = null
    }

    @Parcelize
    data class Audio(
            override val file: File,
            val metadata: AudioMetadata?
    ) : FileEntry(file), AudioMetadataInterface by metadata ?: AudioMetadata.EMPTY {

        @IgnoredOnParcel
        override val image by lazy {
            ArtworkRequestHandler.musicFileUri(file)
        }

        @IgnoredOnParcel
        private val displayTitle by lazy {
            FileUtils.getFileDisplayTitle(
                    FileUtils.getFileSortKey(file)?.substringBeforeLast(
                            "."
                    )
            )
        }

        override fun getDisplayTitle(context: Context) = displayTitle

        override fun getDescription(context: Context): CharSequence? {
            return metadata?.getDescription(context)
        }

        fun toQueueEntry(context: Context) = QueueEntry(
                content = Uri.fromFile(file),
                image = image,
                metadata = QueueEntry.Metadata(
                        title = getDisplayTitle(context),
                        description = getDescription(context),
                        album = metadata?.album,
                        artist = metadata?.artist,
                        genre = metadata?.genre,
                        duration = metadata?.duration ?: -1L
                )
        )
    }
}