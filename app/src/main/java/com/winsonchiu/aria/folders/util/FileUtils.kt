package com.winsonchiu.aria.folders.util

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

        return fileSortKey.drop(startIndex)
    }
}