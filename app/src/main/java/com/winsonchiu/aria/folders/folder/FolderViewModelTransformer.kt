package com.winsonchiu.aria.folders.folder

import android.content.Context
import android.support.annotation.WorkerThread
import com.airbnb.epoxy.EpoxyModel
import com.winsonchiu.aria.R
import com.winsonchiu.aria.folders.util.FileDisplayAndSortMetadata
import com.winsonchiu.aria.folders.util.FileSorter
import com.winsonchiu.aria.folders.util.FileSorter.sortFileItemViewModels
import com.winsonchiu.aria.folders.util.FileUtils
import com.winsonchiu.aria.music.MetadataExtractor
import com.winsonchiu.aria.music.artistDisplayValue
import java.io.File

object FolderViewModelTransformer {

    @WorkerThread
    fun transform(
            context: Context,
            listener: FileItemView.Listener,
            model: FolderController.Model
    ): FileViewModel {
        val (folder, files) = model
        val folderTitle = getFolderTitle(folder)

        if (files.isEmpty()) {
            return FileViewModel(
                    folder,
                    folderTitle,
                    emptyList()
            )
        }

        val firstMetadata = files.first().metadata
        val firstArtist = firstMetadata.artistDisplayValue()
        val firstAlbum = firstMetadata?.album
        val areArtistsEqual = !firstArtist.isNullOrBlank() && files.asSequence()
                .fold(true) { matches, it ->
                    matches && firstArtist == it.metadata.artistDisplayValue()
                }
        val areAlbumsEqual = !firstAlbum.isNullOrBlank() && files.asSequence()
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
        }

        if (!headerText.isNullOrBlank()) {
            epoxyModels += FileSectionHeaderViewModel_()
                    .id(-1)
                    .text(headerText!!)
        }

        files.map(::getFileDisplayAndSortMetadata)

        epoxyModels += files
                .map(::getFileDisplayAndSortMetadata)
                .toList()
                .let { sortFileItemViewModels(it, FileSorter.Method.BY_NAME, false) }
                .map {
                    val (fileMetadata, displayTitle, sortKey) = it
                    val (file, image, metadata) = fileMetadata
                    FileItemViewModel_()
                            .id(file.name)
                            .fileMetadata(fileMetadata)
                            .listener(listener)
                            .title(displayTitle)
                            .description(
                                    getFileDescription(
                                            context,
                                            metadata,
                                            !areArtistsEqual,
                                            !areAlbumsEqual
                                    )
                            )
                }
                .toList()

        return FileViewModel(
                folder,
                folderTitle,
                epoxyModels
        )
    }

    private fun getFileDisplayAndSortMetadata(it: FolderController.FileMetadata): FileDisplayAndSortMetadata {
        val fileSortKey = FileUtils.getFileSortKey(it.file)
        val fileDisplayTitle = FileUtils.getFileDisplayTitle(fileSortKey?.substringBeforeLast("."))
        return FileDisplayAndSortMetadata(it, fileDisplayTitle, fileSortKey)
    }

    private fun getFolderTitle(folder: File) = folder.invariantSeparatorsPath

    private fun getFileDescription(
            context: Context,
            metadata: MetadataExtractor.Metadata?,
            showArtist: Boolean,
            showAlbum: Boolean
    ): String? {
        metadata ?: return null

        val resources = context.resources
        val artist = metadata.artistDisplayValue()
        val album = metadata.album

        fun String?.isShowable() = !isNullOrBlank()
                && !equals("unknown", ignoreCase = true)
                && !equals("null", ignoreCase = true)

        val artistShowable = artist.isShowable() && showArtist
        val albumShowable = album.isShowable() && showAlbum

        return when {
            artistShowable && albumShowable -> resources.getString(
                    R.string.fileDescriptionFormatArtistAndAlbum,
                    artist,
                    album
            )
            artistShowable -> resources.getString(R.string.fileDescriptionFormatArtist, artist)
            albumShowable -> resources.getString(R.string.fileDescriptionFormatAlbum, album)
            else -> null
        }
    }

    data class FileViewModel(
            val folder: File,
            val folderTitle: String?,
            val models: List<EpoxyModel<*>>
    )
}