package com.winsonchiu.aria.source.folders.inner

import android.content.Context
import androidx.annotation.WorkerThread
import com.airbnb.epoxy.EpoxyModel
import com.winsonchiu.aria.framework.text.TextConverter
import com.winsonchiu.aria.source.folders.FileEntry
import com.winsonchiu.aria.source.folders.R
import com.winsonchiu.aria.source.folders.inner.view.FileItemView
import com.winsonchiu.aria.source.folders.inner.view.FileItemViewModel_
import com.winsonchiu.aria.source.folders.inner.view.FileSectionHeaderViewModel_
import com.winsonchiu.aria.source.folders.util.FileSorter
import com.winsonchiu.aria.source.folders.util.FileSorter.sortFileItemViewModels
import com.winsonchiu.aria.source.folders.util.FileUtils
import com.winsonchiu.aria.source.folders.util.FileUtils.getFolderTitle
import java.io.File

object FolderViewModelTransformer {

    @WorkerThread
    fun transform(
            context: Context,
            listener: FileItemView.Listener,
            model: FolderController.Model
    ): FileViewModel {
        val (folder, entries) = model
        val folderTitle = getFolderTitle(folder)

        if (entries.isEmpty()) {
            return FileViewModel(
                    folder,
                    folderTitle,
                    emptyList()
            )
        }

        val firstMetadata = (entries.first() as? FileEntry.Audio)?.metadata
        val firstArtist = firstMetadata?.artistDisplayValue
        val firstAlbum = firstMetadata?.album
        val areArtistsEqual = !firstArtist.isNullOrBlank() && entries.asSequence()
                .filterIsInstance<FileEntry.Audio>()
                .fold(true) { matches, it ->
                    matches && firstArtist == it.metadata?.artistDisplayValue
                }
        val areAlbumsEqual = !firstAlbum.isNullOrBlank() && entries.asSequence()
                .filterIsInstance<FileEntry.Audio>()
                .fold(true) { matches, it ->
                    matches && firstAlbum == it.metadata?.album
                }

        val epoxyModels = mutableListOf<EpoxyModel<*>>()

        val headerText = when {
            areArtistsEqual && areAlbumsEqual -> context.getString(
                    R.string.audioMetadataFormatArtistAndAlbum,
                    firstArtist,
                    firstAlbum
            )
            areArtistsEqual -> context.getString(R.string.audioMetadataFormatArtist, firstArtist)
            areAlbumsEqual -> context.getString(R.string.audioMetadataFormatAlbum, firstAlbum)
            else -> null
        }.let { TextConverter.translate(it, titleCase = true) }

        if (!headerText.isNullOrBlank()) {
            epoxyModels += FileSectionHeaderViewModel_()
                    .id(-1)
                    .text(headerText)
        }

        epoxyModels += entries
                .map(FileUtils::getFileDisplayAndSortMetadata)
                .toList()
                .let { sortFileItemViewModels(it, FileSorter.Method.BY_NAME, false) }
                .map {
                    val (entry, displayTitle, _) = it

                    val description = when (entry) {
                        is FileEntry.Folder,
                        is FileEntry.Playlist -> entry.getDescription(context)
                        is FileEntry.Audio -> entry.metadata?.getDescription(
                                context,
                                !areArtistsEqual,
                                !areAlbumsEqual
                        )
                    }

                    FileItemViewModel_()
                            .id(entry.file.absolutePath)
                            .entry(entry)
                            .listener(listener)
                            .title(displayTitle)
                            .description(description)
                }
                .toList()

        return FileViewModel(
                folder,
                folderTitle,
                epoxyModels
        )
    }

    data class FileViewModel(
            val folder: File,
            val folderTitle: String?,
            val models: List<EpoxyModel<*>>
    )
}