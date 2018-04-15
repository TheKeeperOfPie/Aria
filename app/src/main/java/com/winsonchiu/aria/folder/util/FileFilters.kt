package com.winsonchiu.aria.folder.util

import com.winsonchiu.aria.folder.util.FileFilters.foldersRecursiveNonEmpty
import java.io.File
import java.io.FileFilter
import java.net.URLConnection

infix fun FileFilter.or(fileFilter: FileFilter) = FileFilters.or(this, fileFilter)

fun FileFilter.withFolders() = FileFilters.or(this, foldersRecursiveNonEmpty(this))

object FileFilters {

    val COVER_IMAGE_REGEX = Regex("(folder|cover|album).*", RegexOption.IGNORE_CASE)

    val FOLDERS = FileFilter { file -> file.isDirectory }

    val AUDIO = FileFilter { file ->
        val contentType = tryContentType(file)
        contentType?.startsWith("audio") == true
    }

    val IMAGES = FileFilter { file ->
        val contentType = tryContentType(file)
        contentType?.startsWith("image") == true
    }

    fun foldersRecursiveNonEmpty(fileFilter: FileFilter) = and(FOLDERS, FileFilter { rootFile ->
        rootFile.walkBottomUp().fold(false) { matches, file -> matches || fileFilter.accept(file) }
    })

    fun or(vararg fileFilter: FileFilter) = FileFilter { file ->
        fileFilter.fold(false) { matches, filter -> matches || filter.accept(file) }
    }

    fun and(vararg fileFilter: FileFilter) = FileFilter { file ->
        fileFilter.fold(true) { matches, filter -> matches && filter.accept(file) }
    }

    private fun tryContentType(file: File) = try {
        URLConnection.guessContentTypeFromName(file.name)
    } catch (e: Exception) {
        null
    }
}