package com.winsonchiu.aria.queue

import android.annotation.SuppressLint
import android.content.Context
import com.squareup.moshi.JsonClass
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import okio.Okio
import java.io.FileNotFoundException
import java.util.concurrent.TimeUnit

class QueuePersister(
        context: Context,
        private val mediaQueue: MediaQueue
) {

    companion object {
        private const val FILE_NAME = "queue.json"
    }

    private val context = context.applicationContext

    private val wrapperAdapter = QueueMoshi.moshi.adapter(Wrapper::class.java)

    private val tempQueue = mutableListOf<QueueEntry>()
    private val tempRecord = mutableListOf<QueueOp>()

    @SuppressLint("CheckResult")
    fun initialize() {
        mediaQueue.queueUpdates
                .debounce(3, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .switchMap {
                    Observable.fromCallable {
                        tempQueue.clear()
                        tempRecord.clear()

                        val (queue, currentIndex, currentEntry) = mediaQueue.model
                        val record = mediaQueue.opRecord.record

                        tempQueue.addAll(queue)
                        tempRecord.addAll(record)

                        Wrapper(tempQueue, currentIndex, currentEntry, tempRecord)
                    }
                }
                .subscribe { wrapper ->
                    context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use {
                        Okio.sink(it).use {
                            Okio.buffer(it).use {
                                wrapperAdapter.toJson(it, wrapper)
                            }
                        }
                    }
                }
    }

    fun read(): Wrapper? {
        return try {
            context.openFileInput(FILE_NAME).use {
                Okio.source(it).use {
                    Okio.buffer(it).use {
                        wrapperAdapter.fromJson(it)
                    }
                }
            }
        } catch (e: FileNotFoundException) {
            null
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                throw e
            }
            null
        }
    }

    @JsonClass(generateAdapter = true)
    class Wrapper(
            val queue: List<QueueEntry> = emptyList(),
            val currentIndex: Int = 0,
            val currentEntry: QueueEntry? = null,
            val record: List<QueueOp>
    )
}