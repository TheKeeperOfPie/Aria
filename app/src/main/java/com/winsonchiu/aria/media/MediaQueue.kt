package com.winsonchiu.aria.media

import android.graphics.Bitmap
import com.jakewharton.rxrelay2.BehaviorRelay
import com.winsonchiu.aria.framework.dagger.ApplicationScope
import com.winsonchiu.aria.music.MetadataExtractor
import com.winsonchiu.aria.music.artwork.ArtworkCache
import java.io.File
import java.util.*
import javax.inject.Inject

@ApplicationScope
class MediaQueue @Inject constructor() {

    val queueUpdates = BehaviorRelay.create<List<QueueItem>>()

    private val queue = mutableListOf<QueueItem>()

    private var currentIndex = 0

    fun set(queue: List<QueueItem>, initialItem: QueueItem? = null) {
        set(queue, queue.indexOf(initialItem))
    }

    fun set(queue: List<QueueItem>, initialIndex: Int?) {
        this.queue.clear()
        this.queue.addAll(queue)
        this.currentIndex = initialIndex?.coerceIn(0, queue.size - 1) ?: currentIndex
        queueUpdates.accept(queue)
    }

    fun swap(positionOne: Int, positionTwo: Int) {
        val newList = queue.toMutableList()
        Collections.swap(newList, positionOne, positionTwo)
        set(queue, currentIndex)
    }

    fun currentItem() = queue.getOrNull(currentIndex)
    fun next() = queue.getOrNull(Math.floorMod(++currentIndex, queue.size))
    fun previous() = queue.getOrNull(Math.floorMod(--currentIndex, queue.size))

    data class QueueItem(
            val file: File,
            val image: ArtworkCache.Metadata?,
            val metadata: MetadataExtractor.Metadata?
    )
}