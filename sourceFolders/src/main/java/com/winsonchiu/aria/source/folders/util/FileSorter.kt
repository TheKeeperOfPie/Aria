package com.winsonchiu.aria.source.folders.util

import com.winsonchiu.aria.framework.util.compareOrNull
import com.winsonchiu.aria.source.folders.FileEntry
import java.util.function.Function

object FileSorter {

    enum class Method(val comparator: Comparator<FileEntry>) {
        BY_NAME(caseInsensitiveComparator),
        DEFAULT(defaultComparator)
    }

    private fun String.fromFirstLetterOrDigit(): String {
        val firstLetterOrDigit = this.indexOfFirst { Character.isLetterOrDigit(it) }
        return when {
            firstLetterOrDigit <= 0 || firstLetterOrDigit >= length - 1 -> this
            else -> substring(firstLetterOrDigit, length)
        }
    }

    private val caseInsensitiveComparator = Comparator<FileEntry> { first, second ->
        first.file.name.fromFirstLetterOrDigit().compareTo(second.file.name.fromFirstLetterOrDigit(), ignoreCase = true)
    }

    private val defaultComparator = Comparator<FileEntry> { first, second ->
        compareOrNull(first, second) { (it as? FileEntry.Audio)?.album }
                ?: compareOrNull(first, second) { (it as? FileEntry.Audio)?.cdTrackNumber }
                ?: sortKeyComparator.compare(
                        FileUtils.getFileDisplayAndSortMetadata(first),
                        FileUtils.getFileDisplayAndSortMetadata(second)
                )
    }

    private val sortKeyComparator = Comparator.nullsLast<String> { first, second ->
        first.fromFirstLetterOrDigit().compareTo(second.fromFirstLetterOrDigit(), ignoreCase = true)
    }.let { Comparator.comparing<FileDisplayAndSortMetadata, String?>(Function { it.sortKey }, it) }
}