package com.winsonchiu.aria.source.folder.util

import com.winsonchiu.aria.source.folder.FileEntry

data class FileDisplayAndSortMetadata(
        val entry: FileEntry,
        val displayTitle: String?,
        val sortKey: String?
)