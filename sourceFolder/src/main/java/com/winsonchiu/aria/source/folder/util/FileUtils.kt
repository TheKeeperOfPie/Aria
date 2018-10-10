package com.winsonchiu.aria.source.folder.util

import android.content.Context
import com.winsonchiu.aria.framework.text.TextConverter
import com.winsonchiu.aria.source.folder.FileEntry
import com.winsonchiu.aria.source.folder.R
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
        var foundSeparator = false
        val startIndex = fileSortKey.indexOfFirst {
            when (it) {
                '-', '.' -> {
                    foundSeparator = true
                    return@indexOfFirst false
                }
                ' ' -> foundSeparator = true
            }

            if (foundSeparator) {
                !it.isWhitespace()
            } else {
                !it.isWhitespace() && !it.isDigit()
            }
        }

        val text = if (foundSeparator) {
            fileSortKey.drop(startIndex.coerceAtLeast(0))
        } else {
            fileSortKey
        }

        return TextConverter.translate(text.trim(), titleCase = true)
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
        }.let { TextConverter.translate(it, titleCase = true)}
    }

    fun getFileDisplayAndSortMetadata(entry: FileEntry): FileDisplayAndSortMetadata {
        val fileSortKey = FileUtils.getFileSortKey(entry.file)
        val fileDisplayTitle = FileUtils.getFileDisplayTitle(fileSortKey?.substringBeforeLast("."))
        return FileDisplayAndSortMetadata(entry, fileDisplayTitle, fileSortKey)
    }

    fun getFolderTitle(folder: File) = folder.invariantSeparatorsPath
}