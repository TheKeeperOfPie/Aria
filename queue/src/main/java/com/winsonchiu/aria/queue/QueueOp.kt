package com.winsonchiu.aria.queue

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.Collections

@Suppress("DataClassPrivateConstructor")
sealed class QueueOp : Parcelable {

    abstract fun apply(
            input: List<QueueEntry>,
            currentIndex: Int
    ): Output

    abstract fun reverse(
            input: List<QueueEntry>,
            currentIndex: Int
    ): Output

    data class Output(
            val queue: List<QueueEntry>,
            val currentIndex: Int
    )


    @Parcelize
    data class AddToEnd(private val newEntries: List<QueueEntry>) : QueueOp() {

        constructor(newEntry: QueueEntry) : this(Collections.singletonList(newEntry))

        override fun apply(
                input: List<QueueEntry>,
                currentIndex: Int
        ): Output {
            return Output(input + newEntries, currentIndex)
        }

        override fun reverse(
                input: List<QueueEntry>,
                currentIndex: Int
        ): Output {
            val result = input.dropLast(newEntries.size)
            return Output(result, currentIndex.coerceAtMost(result.size - 1))
        }
    }

    @Parcelize
    data class AddNext private constructor(
            private val newEntries: List<QueueEntry>,
            private var insertedIndex: Int
    ) : QueueOp() {

        constructor(newEntry: QueueEntry) : this(Collections.singletonList(newEntry))

        constructor(newEntries: List<QueueEntry>) : this(newEntries, 0)

        override fun apply(
                input: List<QueueEntry>,
                currentIndex: Int
        ): Output {
            insertedIndex = (currentIndex + 1).coerceAtMost(input.size)
            val result = input.toMutableList().apply {
                newEntries.asReversed().forEach {
                    add(insertedIndex, it)
                }
            }
            return Output(result, currentIndex)
        }

        override fun reverse(
                input: List<QueueEntry>,
                currentIndex: Int
        ): Output {
            val result = input.toMutableList().apply {
                for (count in (0 until newEntries.size)) {
                    removeAt(insertedIndex)
                }
            }
            return Output(result, currentIndex.coerceAtMost(result.size - 1))
        }
    }

    @Parcelize
    data class Shuffle private constructor(
            private var oldInput: List<QueueEntry>?
    ) : QueueOp() {

        constructor() : this(null)

        override fun apply(
                input: List<QueueEntry>,
                currentIndex: Int
        ): Output {
            this.oldInput = input
            return Output(input.shuffled(), 0)
        }

        override fun reverse(
                input: List<QueueEntry>,
                currentIndex: Int
        ): Output {
            val currentItem = input[currentIndex]
            val newIndex = oldInput!!.indexOf(currentItem).coerceIn(0, oldInput!!.size - 1)
            return Output(oldInput!!, newIndex)
        }
    }

    @Parcelize
    data class ReplaceAll private constructor(
            private val newEntries: List<QueueEntry>,
            private var oldInput: List<QueueEntry>?
    ) : QueueOp() {

        constructor(
                newEntry: QueueEntry
        ) : this(Collections.singletonList(newEntry))

        constructor(
                newEntries: List<QueueEntry>
        ) : this(newEntries, null)

        override fun apply(
                input: List<QueueEntry>,
                currentIndex: Int
        ): Output {
            this.oldInput = input
            return Output(newEntries, 0)
        }

        override fun reverse(
                input: List<QueueEntry>,
                currentIndex: Int
        ): Output {
            val currentItem = input[currentIndex]
            val newIndex = oldInput!!.indexOf(currentItem).coerceIn(0, oldInput!!.size - 1)
            return Output(oldInput!!, newIndex)
        }
    }

    @Parcelize
    data class Move(
            private val fromIndex: Int,
            private val toIndex: Int
    ) : QueueOp() {

        override fun apply(
                input: List<QueueEntry>,
                currentIndex: Int
        ): Output {
            val result = input.toMutableList()
            val entry = result.removeAt(fromIndex)
            result.add(toIndex, entry)

            val newIndex = when {
                currentIndex == fromIndex -> toIndex
                currentIndex == toIndex && currentIndex > fromIndex -> currentIndex - 1
                currentIndex == toIndex && currentIndex < fromIndex -> currentIndex + 1
                fromIndex < currentIndex && toIndex < currentIndex -> currentIndex
                fromIndex > currentIndex && toIndex > currentIndex -> currentIndex
                fromIndex < currentIndex -> Math.floorMod(currentIndex - 1, result.size)
                fromIndex > currentIndex -> Math.floorMod(currentIndex + 1, result.size)
                else -> currentIndex
            }.run { Math.floorMod(this, result.size) }

            return Output(result, newIndex)
        }

        override fun reverse(
                input: List<QueueEntry>,
                currentIndex: Int
        ): Output {
            return Move(toIndex, fromIndex).apply(input, currentIndex)
        }
    }

    @Parcelize
    data class Remove private constructor(
            private val removeIndex: Int,
            private var removedEntry: QueueEntry?
    ) : QueueOp() {

        constructor(removeIndex: Int): this(removeIndex, null)

        override fun apply(
                input: List<QueueEntry>,
                currentIndex: Int
        ): Output {
            val result = input.toMutableList()
            removedEntry = result.removeAt(removeIndex)

            val newIndex = if (removeIndex < currentIndex) {
                currentIndex - 1
            } else {
                currentIndex
            }

            return Output(result, newIndex.coerceIn(0, result.size - 1))
        }

        override fun reverse(
                input: List<QueueEntry>,
                currentIndex: Int
        ): Output {
            val result = input.toMutableList()
            result.add(removeIndex, removedEntry!!)

            val newIndex = if (removeIndex <= currentIndex) {
                currentIndex + 1
            } else {
                currentIndex
            }

            return Output(result, newIndex.coerceAtMost(result.size - 1))
        }
    }
}
