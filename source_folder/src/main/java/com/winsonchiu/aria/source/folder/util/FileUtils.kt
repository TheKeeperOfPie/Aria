package com.winsonchiu.aria.source.folder.util

import android.content.Context
import com.winsonchiu.aria.source.folder.R
import com.winsonchiu.aria.source.folder.inner.FolderController
import java.io.File

object FileUtils {

    fun getFileSortKey(file: File): String? {
        val tags = mutableListOf<Char>()
        val remaining = mutableListOf<Char>()
        var insideBracket = false
        var finished = false
        file.name.forEach {
            if (finished) {
                remaining += it
            } else {
                when (it) {
                    '[' -> insideBracket = true
                    ']' -> insideBracket = false
                }

                when {
                    it == '[' || it == ']' || insideBracket -> tags += it
                    else -> {
                        remaining += it
                        if (!it.isWhitespace() && !it.isDigit() && it != '-') {
                            finished = true
                        }
                    }
                }
            }
        }

        return String(remaining.toCharArray()).trim()
    }

    fun getFileDisplayTitle(fileSortKey: String?): String? {
        fileSortKey ?: return null
        val startIndex = fileSortKey.indexOfFirst {
            when (it) {
                '-', '.' -> return@indexOfFirst false
            }

            !it.isDigit() && !it.isWhitespace()
        }

        return fileSortKey.drop(startIndex.coerceAtLeast(0))
    }

    fun getFileDescription(
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

    fun getFileDisplayAndSortMetadata(it: FolderController.FileMetadata): FileDisplayAndSortMetadata {
        val fileSortKey = FileUtils.getFileSortKey(it.file)
        val fileDisplayTitle = FileUtils.getFileDisplayTitle(fileSortKey?.substringBeforeLast("."))
        return FileDisplayAndSortMetadata(it, fileDisplayTitle, fileSortKey)
    }

    fun getFolderTitle(folder: File) = folder.invariantSeparatorsPath
}