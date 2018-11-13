package com.winsonchiu.aria.source.folders.inner

import android.annotation.SuppressLint
import android.app.Application
import android.os.Environment
import androidx.fragment.app.Fragment
import com.jakewharton.rxrelay2.BehaviorRelay
import com.winsonchiu.aria.framework.async.RequestState
import com.winsonchiu.aria.framework.dagger.fragment.FragmentLifecycleBoundComponent
import com.winsonchiu.aria.framework.util.Failsafe
import com.winsonchiu.aria.framework.util.FileFilters
import com.winsonchiu.aria.framework.util.or
import com.winsonchiu.aria.framework.util.withFolders
import com.winsonchiu.aria.queue.MediaQueue
import com.winsonchiu.aria.queue.QueueEntry
import com.winsonchiu.aria.queue.QueueOp
import com.winsonchiu.aria.source.folders.FileEntry
import com.winsonchiu.aria.source.folders.util.FileSorter
import com.winsonchiu.aria.source.folders.util.MetadataExtractor
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import java.io.File
import java.util.Collections
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@FolderFragmentScope
class FolderController @Inject constructor(
        private val application: Application,
        private val metadataExtractor: MetadataExtractor,
        private val mediaQueue: MediaQueue
) : FragmentLifecycleBoundComponent() {

    val folderContents = BehaviorRelay.create<Model>()
    val state = BehaviorRelay.createDefault<RequestState>(RequestState.NONE)

    private val stateChange = BehaviorRelay.createDefault<RequestState>(RequestState.NONE)

    private val folder by arg(FolderFragment.Args.folder)

    private val folderFile = Single.fromCallable {
        if (folder.isNullOrBlank()) {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        } else {
            File(folder)
        }
    }.cache()

    private val refreshRelay = BehaviorRelay.createDefault(System.currentTimeMillis())

    @SuppressLint("CheckResult")
    override fun onFirstInitialize(fragment: Fragment) {
        super.onFirstInitialize(fragment)

        stateChange.debounce(500, TimeUnit.MILLISECONDS)
                .bindToLifecycle()
                .subscribe(state)

        Observables.combineLatest(refreshRelay, folderFile.toObservable())
                .switchMapSingle { (_, folder) ->
                    Single.fromCallable {
                        val entries = (folder.listFiles() ?: emptyArray())
                                .filter { (FileFilters.PLAYLIST or FileFilters.AUDIO).withFolders().accept(it) }
                                .mapNotNull {
                                    when {
                                        it.isDirectory -> FileEntry.Folder(it)
                                        FileFilters.PLAYLIST.accept(it) -> FileEntry.Playlist(it)
                                        FileFilters.AUDIO.accept(it) -> FileEntry.Audio(
                                                it,
                                                metadataExtractor.extract(it)
                                        )
                                        else -> null
                                    }
                                }
                                .toList()
                                .sortedWith(FileSorter.Method.DEFAULT.comparator)

                        Model(folder, entries)
                    }
                            .doOnSubscribe { stateChange.accept(RequestState.LOADING) }
                            .doFinally { stateChange.accept(RequestState.DONE) }
                }
                .bindToLifecycle()
                .subscribe(folderContents)
    }

    fun refresh() {
        refreshRelay.accept(System.currentTimeMillis())
    }

    fun playNext(entry: FileEntry) {
        mediaQueue.push(QueueOp.AddNext(buildEntries(entry)))
    }

    fun addToQueue(entry: FileEntry) {
        mediaQueue.push(QueueOp.AddToEnd(buildEntries(entry)))
    }

    private fun buildEntries(entry: FileEntry): List<QueueEntry> {
        return when (entry) {
            is FileEntry.Folder -> {
                entry.file.walkTopDown()
                        .filter { FileFilters.AUDIO.accept(it) }
                        .map { FileEntry.Audio(it, metadataExtractor.extract(it)) }
                        .sortedWith(FileSorter.Method.DEFAULT.comparator)
                        .map { it.toQueueEntry(application) }
                        .toList()
            }
            is FileEntry.Playlist -> {
                entry.file.useLines {
                    it.filterNot { it.startsWith("#") }
                            .mapNotNull { Failsafe.orNull { entry.file.resolve(it) } }
                            .filter { it.exists() && FileFilters.AUDIO.accept(it) }
                            .map { FileEntry.Audio(it, metadataExtractor.extract(it)) }
                            .sortedWith(FileSorter.Method.DEFAULT.comparator)
                            .mapNotNull { it.toQueueEntry(application) }
                            .toList()
                }
            }
            is FileEntry.Audio -> {
                Collections.singletonList(entry.toQueueEntry(application))
            }
        }
    }

    data class Model(
            val folder: File,
            val entries: List<FileEntry>
    )
}
