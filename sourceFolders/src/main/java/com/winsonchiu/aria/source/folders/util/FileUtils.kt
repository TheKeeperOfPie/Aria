package com.winsonchiu.aria.source.folders.util

import com.winsonchiu.aria.framework.text.TextConverter
import com.winsonchiu.aria.source.folders.FileEntry
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

        return TextConverter.translate(text.trim())
    }

    fun getFileDisplayAndSortMetadata(entry: FileEntry): FileDisplayAndSortMetadata {
        val fileSortKey = FileUtils.getFileSortKey(entry.file)
        val fileDisplayTitle = FileUtils.getFileDisplayTitle(fileSortKey?.substringBeforeLast("."))
        return FileDisplayAndSortMetadata(fileDisplayTitle, fileSortKey)
    }

    fun getFolderTitle(folder: File) = folder.invariantSeparatorsPath
}