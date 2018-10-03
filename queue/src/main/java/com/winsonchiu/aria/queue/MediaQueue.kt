package com.winsonchiu.aria.queue

import android.annotation.SuppressLint
import android.app.Application
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import java.util.LinkedList
import javax.inject.Inject

@QueueScope
class MediaQueue @Inject constructor(
        application: Application
) {

    val queueUpdates = BehaviorRelay.createDefault(Model())

    internal val model
        get() = queueUpdates.value

    private val queuePersister: QueuePersister = QueuePersister(application, this)

    internal var opRecord = OpRecord()

    init {
        initialize()
    }

    @SuppressLint("CheckResult")
    private fun initialize() {
        queuePersister.initialize()
        queuePersister.read()
                .subscribe {
                    opRecord.record.addAll(it.record)
                    queueUpdates.accept(
                            Model(
                                    queue = it.queue,
                                    currentIndex = it.currentIndex,
                                    currentEntry = it.currentEntry
                            )
                    )
                }
    }

    fun currentItem() = model.currentEntry

    fun next(): QueueEntry? {
        moveToIndex(model.currentIndex + 1)
        return model.currentEntry
    }

    fun previous(): QueueEntry? {
        moveToIndex(model.currentIndex - 1)
        return model.currentEntry
    }

    private fun moveToIndex(index: Int) {
        val queue = model.queue
        val currentIndex = Math.floorMod(index, queue.size)
        val currentItem = queue.getOrNull(currentIndex)
        queueUpdates.accept(Model(queue, currentIndex, currentItem))
    }

    fun setCurrentItem(queueEntry: QueueEntry) {
        val queue = model.queue
        val currentIndex = queue.indexOf(queueEntry).coerceAtLeast(0)
        val currentItem = queue.getOrNull(currentIndex)
        queueUpdates.accept(Model(queue, currentIndex, currentItem))
    }

    fun push(op: QueueOp) {
        opRecord.push(op)
        queueUpdates.accept(Model(op.apply(model.queue, model.currentIndex)))
    }

    fun pop() {
        val op = opRecord.pop() ?: return
        queueUpdates.accept(Model(op.reverse(model.queue, model.currentIndex)))
    }

    data class Model(
            val queue: List<QueueEntry> = emptyList(),
            val currentIndex: Int = 0,
            val currentEntry: QueueEntry? = null
    ) {

        constructor(output: QueueOp.Output) : this(
                output.queue,
                output.currentIndex,
                output.queue.getOrNull(output.currentIndex)
        )

        fun copy() = copy(queue = queue.toList(), currentEntry = currentEntry?.copy(), currentIndex = currentIndex)
    }

    internal class OpRecord(
            private val maxSize: Int = DEFAULT_SIZE
    ) {

        companion object {
            private const val DEFAULT_SIZE = 150
        }

        val record = LinkedList<QueueOp>()

        fun push(op: QueueOp) {
            // Merge moves of the same target
            if (op is QueueOp.Move) {
                val lastOp = record.peek()
                if (lastOp is QueueOp.Move && lastOp.toIndex == op.fromIndex) {
                    record.pop()
                    record.push(QueueOp.Move(lastOp.fromIndex, op.toIndex))
                    return
                }
            }

            record.push(op)
            if (record.size > maxSize) {
                record.removeLast()
            }
        }

        fun pop(): QueueOp? {
            return record.poll()
        }
    }
}
