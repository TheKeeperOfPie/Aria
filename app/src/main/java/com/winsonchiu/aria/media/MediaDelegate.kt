package com.winsonchiu.aria.media

import com.jakewharton.rxrelay2.BehaviorRelay
import com.winsonchiu.aria.framework.dagger.ApplicationScope
import com.winsonchiu.aria.folders.folder.FolderController
import javax.inject.Inject

@ApplicationScope
class MediaDelegate @Inject constructor() {

    val playActions = BehaviorRelay.create<List<FolderController.FileMetadata>>()

    fun play(fileMetadata: FolderController.FileMetadata) {
        playActions.accept(listOf(fileMetadata))
    }

    fun play(fileMetadata: List<FolderController.FileMetadata>) {
        playActions.accept(fileMetadata)
    }
}