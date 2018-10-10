package com.winsonchiu.aria.source.folder

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import com.winsonchiu.aria.artwork.ArtworkRequestHandler
import com.winsonchiu.aria.queue.QueueEntry
import com.winsonchiu.aria.source.folder.util.FileUtils
import com.winsonchiu.aria.source.folder.util.MetadataExtractor
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.io.File

sealed class FileEntry(
        open val file: File
): Parcelable {
    abstract val image: Uri?

    abstract val title: CharSequence?

    abstract fun description(context: Context): CharSequence?

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
        override val title by lazy {
            FileUtils.getFileDisplayTitle(
                    FileUtils.getFileSortKey(file)?.substringBeforeLast(
                            "."
                    )
            )
        }

        override fun description(context: Context): CharSequence? = null
    }

    @Parcelize
    data class Playlist(
            override val file: File
    ) : FileEntry(file) {

        @IgnoredOnParcel
        override val image: Uri? = null

        @IgnoredOnParcel
        override val title by lazy {
            FileUtils.getFileDisplayTitle(
                    FileUtils.getFileSortKey(file)?.substringBeforeLast(
                            "."
                    )
            )
        }

        override fun description(context: Context): CharSequence? = null
    }

    @Parcelize
    data class Audio(
            override val file: File,
            val metadata: MetadataExtractor.Metadata?
    ) : FileEntry(file) {

        @IgnoredOnParcel
        override val image by lazy {
            ArtworkRequestHandler.musicFileUri(file)
        }

        @IgnoredOnParcel
        override val title by lazy {
            FileUtils.getFileDisplayTitle(
                    FileUtils.getFileSortKey(file)?.substringBeforeLast(
                            "."
                    )
            )
        }

        @IgnoredOnParcel
        private var description: CharSequence? = null

        override fun description(context: Context): CharSequence? {
            if (description == null) {
                description = FileUtils.getFileDescription(context, metadata, true, true)
            }

            return description
        }

        fun toQueueEntry(context: Context) = QueueEntry(
                content = Uri.fromFile(file),
                image = image,
                metadata = QueueEntry.Metadata(
                        title = title,
                        description = description(context),
                        album = metadata?.album,
                        artist = metadata?.artist,
                        genre = metadata?.genre,
                        duration = metadata?.duration ?: -1L
                )
        )
    }
}