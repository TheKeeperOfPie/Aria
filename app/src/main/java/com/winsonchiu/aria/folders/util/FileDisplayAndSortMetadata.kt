package com.winsonchiu.aria.folders.util

import com.winsonchiu.aria.folders.folder.FolderController

data class FileDisplayAndSortMetadata(
        val fileMetadata: FolderController.FileMetadata,
        val displayTitle: String?,
        val sortKey: String?
)