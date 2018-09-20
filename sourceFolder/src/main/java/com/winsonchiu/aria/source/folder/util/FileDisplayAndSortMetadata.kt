package com.winsonchiu.aria.source.folder.util

import com.winsonchiu.aria.source.folder.inner.FolderController

data class FileDisplayAndSortMetadata(
        val fileMetadata: FolderController.FileMetadata,
        val displayTitle: String?,
        val sortKey: String?
)