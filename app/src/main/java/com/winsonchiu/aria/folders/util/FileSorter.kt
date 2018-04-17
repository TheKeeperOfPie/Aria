package com.winsonchiu.aria.folders.util

import com.winsonchiu.aria.folders.util.FileSorter.Method.BY_NAME
import com.winsonchiu.aria.folders.util.FileSorter.Method.DEFAULT
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

    private val caseInsensitiveSorter = Comparator<File> { first, second ->
        first.name.fromFirstLetterOrDigit().compareTo(second.name.fromFirstLetterOrDigit(), ignoreCase = true)
    }

    private val fileDisplayAndSortMetadataSorter = Comparator.nullsLast<String>({ first, second ->
        first.fromFirstLetterOrDigit().compareTo(second.fromFirstLetterOrDigit(), ignoreCase = true)
    }).let { Comparator.comparing<FileDisplayAndSortMetadata, String?>(Function { it.sortKey }, it) }

    fun sort(files: List<File>, method: Method, reverse: Boolean = false): List<File> {
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

    fun sortFileItemViewModels(files: List<FileDisplayAndSortMetadata>, method: Method, reverse: Boolean = false): List<FileDisplayAndSortMetadata> {
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