package com.winsonchiu.aria.queue

import android.os.Parcelable
import com.squareup.moshi.JsonClass
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
    @JsonClass(generateAdapter = true)
    data class AddToEnd(internal val newEntries: List<QueueEntry>) : QueueOp() {

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
    @JsonClass(generateAdapter = true)
    data class AddNext internal constructor(
            internal val newEntries: List<QueueEntry>,
            internal var insertedIndex: Int
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
    @JsonClass(generateAdapter = true)
    data class Shuffle internal constructor(
            internal var indexMap: MutableMap<Int, Int?>
    ) : QueueOp() {
        constructor() : this(mutableMapOf())

        override fun apply(
                input: List<QueueEntry>,
                currentIndex: Int
        ): Output {

            val indexes = input.asSequence()
                    .mapIndexed { index, _ -> index }
                    .toMutableList()

            indexes.shuffle()

            val first = indexes.subList(0, indexes.size / 2)
            val second = indexes.subList(indexes.size / 2, indexes.size)

            second.forEachIndexed { indexInSublist, indexData ->
                indexMap[indexData] = first.getOrNull(indexInSublist)
            }

            val currentEntry = input.getOrNull(currentIndex)

            if (currentEntry != null) {
                val currentIndexEntry = indexMap.entries.find { it.key == currentIndex || it.value == currentIndex }!!

                if (currentIndexEntry.key != 0 || currentIndexEntry.value != 0) {
                    val currentIndexTarget = if (currentIndexEntry.key == currentIndex) {
                        currentIndexEntry.value ?: currentIndexEntry.key
                    } else {
                        currentIndexEntry.key
                    }

                    val zeroEntry = indexMap.entries.find { it.key == 0 || it.value == 0 }!!
                    if (zeroEntry.key == 0) {
                        val zeroTarget = zeroEntry.value
                        indexMap.remove(0)
                        indexMap[currentIndexTarget] = zeroTarget
                    } else {
                        indexMap[zeroEntry.key] = currentIndexTarget
                    }

                    if (currentIndexEntry.key == currentIndex) {
                        indexMap[currentIndex] = 0
                    } else {
                        indexMap[0] = currentIndex
                    }
                }
            }

            val result = input.toMutableList()

            indexMap.entries.forEach { (key, value) ->
                if (value != null) {
                    Collections.swap(result, key, value)
                }
            }

            return Output(result, 0)
        }

        override fun reverse(
                input: List<QueueEntry>,
                currentIndex: Int
        ): Output {
            val result = input.toMutableList()

            indexMap.entries.forEach { (key, value) ->
                if (value != null) {
                    Collections.swap(result, value, key)
                }
            }

            val currentItem = input[currentIndex]
            val newIndex = result.indexOf(currentItem).coerceIn(-1, result.size - 1)
            return Output(result, newIndex)
        }
    }

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class ReplaceAll internal constructor(
            internal val newEntries: List<QueueEntry>,
            internal var oldInput: List<QueueEntry>?
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
            val newIndex = oldInput!!.indexOf(currentItem).coerceIn(-1, oldInput!!.size - 1)
            return Output(oldInput!!, newIndex)
        }
    }

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class Move(
            internal val fromIndex: Int,
            internal val toIndex: Int
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
    @JsonClass(generateAdapter = true)
    data class Remove internal constructor(
            internal val removeIndex: Int,
            internal var removedEntry: QueueEntry?
    ) : QueueOp() {

        constructor(removeIndex: Int) : this(removeIndex, null)

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

            return Output(result, newIndex.coerceIn(-1, result.size - 1))
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
