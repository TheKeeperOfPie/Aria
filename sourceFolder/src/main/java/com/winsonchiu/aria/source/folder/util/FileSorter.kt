package com.winsonchiu.aria.source.folder.util

import com.winsonchiu.aria.source.folder.FileEntry
import com.winsonchiu.aria.source.folder.util.FileSorter.Method.BY_NAME
import com.winsonchiu.aria.source.folder.util.FileSorter.Method.DEFAULT
import java.io.File
import java.util.function.Function

object FileSorter {

    enum class Method {
        BY_NAME, DEFAULT
    }

    private fun String.fromFirstLetterOrDigit(): String {
        val firstLetterOrDigit = this.indexOfFirst { Character.isLetterOrDigit(it) }
        return when {
            firstLetterOrDigit <= 0 || firstLetterOrDigit >= length - 1 -> this
            else -> substring(firstLetterOrDigit, length)
        }
    }

    private val caseInsensitiveSorter = Comparator<FileEntry> { first, second ->
        first.file.name.fromFirstLetterOrDigit().compareTo(second.file.name.fromFirstLetterOrDigit(), ignoreCase = true)
    }

    private val fileDisplayAndSortMetadataSorter = Comparator.nullsLast<String> { first, second ->
        first.fromFirstLetterOrDigit().compareTo(second.fromFirstLetterOrDigit(), ignoreCase = true)
    }.let { Comparator.comparing<FileDisplayAndSortMetadata, String?>(Function { it.sortKey }, it) }

    fun sort(
            files: List<FileEntry>,
            method: Method,
            reverse: Boolean = false
    ): List<FileEntry> {
        return if (reverse) {
            when (method) {
                BY_NAME -> files.sortedWith(caseInsensitiveSorter.reversed())
                DEFAULT -> files.reversed()
            }
        } else {
            when (method) {
                BY_NAME -> files.sortedWith(caseInsensitiveSorter)
                DEFAULT -> files
            }
        }
    }

    fun sortFileItemViewModels(
            files: List<FileDisplayAndSortMetadata>,
            method: Method,
            reverse: Boolean = false
    ): List<FileDisplayAndSortMetadata> {
        return if (reverse) {
            when (method) {
                BY_NAME -> files.sortedWith(fileDisplayAndSortMetadataSorter.reversed())
                DEFAULT -> files.reversed()
            }
        } else {
            when (method) {
                BY_NAME -> files.sortedWith(fileDisplayAndSortMetadataSorter)
                DEFAULT -> files
            }
        }
    }
}