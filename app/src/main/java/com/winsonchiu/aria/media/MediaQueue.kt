package com.winsonchiu.aria.media

import com.jakewharton.rxrelay2.BehaviorRelay
import com.winsonchiu.aria.dagger.ApplicationScope
import com.winsonchiu.aria.music.MetadataExtractor
import java.io.File
import javax.inject.Inject

@ApplicationScope
class MediaQueue @Inject constructor() {

    val queueUpdates = BehaviorRelay.create<List<QueueItem>>()

    private val queue = mutableListOf<QueueItem>()

    private var currentIndex = 0

    fun set(queue: List<QueueItem>, initialItem: QueueItem) {
        set(queue, queue.indexOf(initialItem))
    }

    fun set(queue: List<QueueItem>, initialIndex: Int) {
        this.queue.clear()
        this.queue.addAll(queue)
        this.currentIndex = initialIndex
        queueUpdates.accept(queue)
    }

    fun currentItem() = queue.getOrNull(currentIndex)
    fun next() = queue.getOrNull(Math.floorMod(++currentIndex, queue.size))
    fun previous() = queue.getOrNull(Math.floorMod(--currentIndex, queue.size))

    data class QueueItem(
            val file: File,
            val metadata: MetadataExtractor.Metadata?
    )
}