package com.winsonchiu.aria.folders.folder

import android.content.Context
import android.support.annotation.WorkerThread
import com.airbnb.epoxy.EpoxyModel
import com.winsonchiu.aria.R
import com.winsonchiu.aria.music.MetadataExtractor
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
                    null,
                    emptyList()
            )
        }

        val firstDescription = getFileDescription(context, files.first().metadata)
        val areDescriptionsEqual = files.asSequence()
                .fold(true) { matches, it ->
                    matches && firstDescription == getFileDescription(
                            context,
                            it.metadata
                    )
                }

        val epoxyModels = mutableListOf<EpoxyModel<*>>()

        if (!firstDescription.isNullOrBlank() && areDescriptionsEqual) {
            epoxyModels += FileSectionHeaderViewModel_()
                    .id(-1)
                    .text(firstDescription!!)
        }

        epoxyModels += files.map {
            FileItemViewModel_()
                    .id(it.file.name)
                    .file(it.file)
                    .image(it.image)
                    .listener(listener)
                    .apply {
                        if (!areDescriptionsEqual) {
                            description(
                                    getFileDescription(
                                            context,
                                            it.metadata
                                    )
                            )
                        }
                    }
        }

        val folderDescription = if (areDescriptionsEqual) null else firstDescription

        return FileViewModel(
                folder,
                folderTitle,
                folderDescription,
                epoxyModels
        )
    }

    private fun getFolderTitle(folder: File) = folder.invariantSeparatorsPath

    private fun getFileDescription(context: Context, metadata: MetadataExtractor.Metadata?): String? {
        metadata ?: return null

        val resources = context.resources
        val author = metadata.author
        val album = metadata.album

        fun String?.isShowable() = !isNullOrBlank()
                && !equals("unknown", ignoreCase = true)
                && !equals("null", ignoreCase = true)

        return when {
            author.isShowable() && album.isShowable() -> resources.getString(
                    R.string.fileDescriptionFormatAuthorAlbum,
                    author,
                    album
            )
            author.isShowable() -> resources.getString(R.string.fileDescriptionFormatAuthor, author)
            album.isShowable() -> resources.getString(R.string.fileDescriptionFormatAlbum, album)
            else -> null
        }
    }

    data class FileViewModel(
            val folder: File,
            val folderTitle: String?,
            val folderDescription: String?,
            val models: List<EpoxyModel<*>>
    )
}