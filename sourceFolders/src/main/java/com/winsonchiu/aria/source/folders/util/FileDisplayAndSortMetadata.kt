package com.winsonchiu.aria.source.folders.util

import com.winsonchiu.aria.source.folders.FileEntry

data class FileDisplayAndSortMetadata(
        val entry: FileEntry,
        val displayTitle: String?,
        val sortKey: String?
)