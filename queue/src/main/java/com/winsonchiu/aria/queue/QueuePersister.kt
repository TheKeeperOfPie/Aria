package com.winsonchiu.aria.queue

import android.annotation.SuppressLint
import android.content.Context
import com.squareup.moshi.JsonClass
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import okio.Okio
import java.io.FileNotFoundException
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class QueuePersister(
        context: Context,
        private val mediaQueue: MediaQueue
) {

    companion object {
        private const val FILE_NAME_DEBUG_JSON = "queue.json"
        private const val FILE_NAME_COMPRESSED = "queueCompressed"
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
                    if (BuildConfig.DEBUG) {
                        context.openFileOutput(FILE_NAME_DEBUG_JSON, Context.MODE_PRIVATE).use {
                            Okio.sink(it).use {
                                Okio.buffer(it).use {
                                    wrapperAdapter.toJson(it, wrapper)
                                }
                            }
                        }
                    }

                    context.openFileOutput(FILE_NAME_COMPRESSED, Context.MODE_PRIVATE).use {
                        GZIPOutputStream(it).use {
                            Okio.sink(it).use {
                                Okio.buffer(it).use {
                                    wrapperAdapter.toJson(it, wrapper)
                                }
                            }
                        }
                    }
                }
    }

    fun read(): Maybe<Wrapper> = Maybe.fromCallable<Wrapper> {
        try {
            context.openFileInput(FILE_NAME_COMPRESSED).use {
                GZIPInputStream(it).use {
                    Okio.source(it).use {
                        Okio.buffer(it).use {
                            wrapperAdapter.fromJson(it)
                        }
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
            .subscribeOn(Schedulers.io())

    @JsonClass(generateAdapter = true)
    class Wrapper(
            val queue: List<QueueEntry> = emptyList(),
            val currentIndex: Int = 0,
            val currentEntry: QueueEntry? = null,
            val record: List<QueueOp>
    )
}