package com.winsonchiu.aria.media

import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import com.winsonchiu.aria.framework.dagger.ApplicationScope
import com.winsonchiu.aria.music.MetadataExtractor
import com.winsonchiu.aria.music.artwork.ArtworkCache
import java.io.File
import javax.inject.Inject

@ApplicationScope
class MediaQueue @Inject constructor() {

    val queueUpdates = BehaviorRelay.create<Model>()

    val playPauseActions = PublishRelay.create<Unit>()

    private val queue = mutableListOf<QueueItem>()

    private var currentItem: QueueItem? = null

    private var currentIndex = 0

    fun set(queue: List<QueueItem>, initialItem: QueueItem? = null) {
        this.queue.clear()
        this.queue.addAll(queue)
        setItemAndEmit(initialItem)
    }

    fun add(items: List<QueueItem>, currentItem: QueueItem? = null) {
        this.queue.addAll(items)
        setItemAndEmit(currentItem)
    }

    fun add(item: QueueItem, currentItem: QueueItem? = null) {
        this.queue.add(item)
        setItemAndEmit(currentItem)
    }

    private fun setItemAndEmit(item: QueueItem?) {
        if (item != null) {
            val index = queue.indexOf(item)
            if (index >= 0) {
                currentIndex = index
                currentItem = item
            }
        } else if (currentItem == null) {
            currentItem = queue.getOrNull(currentIndex)
        }

        queueUpdates.accept(Model(queue.toList(), currentItem, currentIndex))
    }

    fun currentItem() = currentItem

    fun next(): QueueItem? {
        moveToIndex(currentIndex + 1)
        return currentItem
    }

    fun previous(): QueueItem? {
        moveToIndex(currentIndex - 1)
        return currentItem
    }

    private fun moveToIndex(index: Int) {
        currentIndex = Math.floorMod(index, queue.size)
        currentItem = queue.getOrNull(currentIndex)
        queueUpdates.accept(Model(queue.toList(), currentItem, currentIndex))
    }

    fun setCurrentItem(queueItem: QueueItem) {
        currentIndex = queue.indexOf(queueItem).coerceAtLeast(0)
        currentItem = queue.getOrNull(currentIndex)
        queueUpdates.accept(Model(queue.toList(), queueItem, currentIndex))
    }

    data class QueueItem(
            val file: File,
            val image: ArtworkCache.Metadata?,
            val metadata: MetadataExtractor.Metadata?,
            val timeAddedToQueue: Long = System.currentTimeMillis()
    )

    data class Model(
            val queue: List<QueueItem> = emptyList(),
            val currentItem: QueueItem? = null,
            val currentIndex: Int = 0
    ) {

        fun copy() = copy(queue = queue.toList(), currentItem = currentItem?.copy(), currentIndex = currentIndex)
    }
}
