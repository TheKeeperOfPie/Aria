package com.winsonchiu.aria.source.folder.inner

import android.content.Context
import androidx.annotation.WorkerThread
import com.airbnb.epoxy.EpoxyModel
import com.winsonchiu.aria.framework.text.TextConverter
import com.winsonchiu.aria.source.folder.FileEntry
import com.winsonchiu.aria.source.folder.R
import com.winsonchiu.aria.source.folder.inner.view.FileItemView
import com.winsonchiu.aria.source.folder.inner.view.FileItemViewModel_
import com.winsonchiu.aria.source.folder.inner.view.FileSectionHeaderViewModel_
import com.winsonchiu.aria.source.folder.util.FileSorter
import com.winsonchiu.aria.source.folder.util.FileSorter.sortFileItemViewModels
import com.winsonchiu.aria.source.folder.util.FileUtils
import com.winsonchiu.aria.source.folder.util.FileUtils.getFolderTitle
import com.winsonchiu.aria.source.folder.util.artistDisplayValue
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
        val firstArtist = firstMetadata.artistDisplayValue()
        val firstAlbum = firstMetadata?.album
        val areArtistsEqual = !firstArtist.isNullOrBlank() && entries.asSequence()
                .filterIsInstance<FileEntry.Audio>()
                .fold(true) { matches, it ->
                    matches && firstArtist == it.metadata.artistDisplayValue()
                }
        val areAlbumsEqual = !firstAlbum.isNullOrBlank() && entries.asSequence()
                .filterIsInstance<FileEntry.Audio>()
                .fold(true) { matches, it ->
                    matches && firstAlbum == it.metadata?.album
                }

        val epoxyModels = mutableListOf<EpoxyModel<*>>()

        val headerText = when {
            areArtistsEqual && areAlbumsEqual -> context.getString(
                    R.string.fileDescriptionFormatArtistAndAlbum,
                    firstArtist,
                    firstAlbum
            )
            areArtistsEqual -> context.getString(R.string.fileDescriptionFormatArtist, firstArtist)
            areAlbumsEqual -> context.getString(R.string.fileDescriptionFormatAlbum, firstAlbum)
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
                        is FileEntry.Playlist -> entry.description(context)
                        is FileEntry.Audio -> FileUtils.getFileDescription(
                                context,
                                entry.metadata,
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