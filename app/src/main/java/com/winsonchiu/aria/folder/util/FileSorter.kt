package com.winsonchiu.aria.folder.util

import com.winsonchiu.aria.folder.util.FileSorter.Method.BY_NAME
import com.winsonchiu.aria.folder.util.FileSorter.Method.DEFAULT
import java.io.File

object FileSorter {

    enum class Method {
        BY_NAME, DEFAULT
    }

    private val caseInsensitiveSorter = Comparator<File> { first, second ->
        first.name.compareTo(second.name, ignoreCase = true)
    }

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
}