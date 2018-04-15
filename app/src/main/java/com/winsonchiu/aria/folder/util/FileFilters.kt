package com.winsonchiu.aria.folder.util

import java.io.FileFilter
import java.net.URLConnection

infix fun FileFilter.and(fileFilter: FileFilter) = FileFilters.combine(this, fileFilter)

object FileFilters {

    val COVER_IMAGE_REGEX = Regex("(folder|cover|album).*", RegexOption.IGNORE_CASE)

    val FOLDERS = FileFilter { file -> file.isDirectory }

    val AUDIO = FileFilter { file ->
        val contentType = URLConnection.guessContentTypeFromName(file.name)
        contentType?.startsWith("audio") == true
    }

    val IMAGES = FileFilter { file ->
        val contentType = URLConnection.guessContentTypeFromName(file.name)
        contentType?.startsWith("image") == true
    }

    fun combine(vararg fileFilter: FileFilter) = FileFilter { file ->
        fileFilter.fold(false) { matches, filter -> matches || filter.accept(file) }
    }
}