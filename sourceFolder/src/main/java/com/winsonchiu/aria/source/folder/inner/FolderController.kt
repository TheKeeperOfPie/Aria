package com.winsonchiu.aria.source.folder.inner

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.fragment.app.Fragment
import com.jakewharton.rxrelay2.BehaviorRelay
import com.winsonchiu.aria.artwork.ArtworkRequestHandler
import com.winsonchiu.aria.framework.async.RequestState
import com.winsonchiu.aria.framework.dagger.fragment.FragmentLifecycleBoundComponent
import com.winsonchiu.aria.framework.util.FileFilters
import com.winsonchiu.aria.framework.util.withFolders
import com.winsonchiu.aria.queue.MediaQueue
import com.winsonchiu.aria.queue.QueueEntry
import com.winsonchiu.aria.queue.QueueOp
import com.winsonchiu.aria.source.folder.util.FileSorter
import com.winsonchiu.aria.source.folder.util.FileUtils
import com.winsonchiu.aria.source.folder.util.MetadataExtractor
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import java.io.File
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

    init {
        subscribe()
    }

    @SuppressLint("CheckResult")
    private fun subscribe() {
        stateChange.debounce(500, TimeUnit.MILLISECONDS)
                .subscribe(state)
    }

    @SuppressLint("CheckResult")
    override fun onFirstInitialize(fragment: Fragment) {
        super.onFirstInitialize(fragment)

        Observables.combineLatest(refreshRelay, folderFile.toObservable())
                .switchMapSingle { (_, folder) ->
                    Single.fromCallable {
                        val files = (folder.listFiles(FileFilters.AUDIO.withFolders()) ?: emptyArray())
                                .toList()
                                .let { FileSorter.sort(it, FileSorter.Method.BY_NAME) }
                                .map { FileMetadata(it, metadataExtractor.extract(it)) }

                        Model(folder, files)
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

    fun playNext(file: File) {
        if (file.isDirectory) {
            val newEntries = file.walkTopDown()
                    .filter { FileFilters.AUDIO.accept(it) }
                    .map { FileMetadata(it, metadataExtractor.extract(it)) }
                    .map { it.toQueueEntry(application) }
                    .toList()

            mediaQueue.push(QueueOp.AddNext(newEntries))
        } else {
            val metadata = folderContents.value.files.first { it.file == file }
            mediaQueue.push(QueueOp.AddNext(metadata.toQueueEntry(application)))
        }
    }

    fun addToQueue(file: File) {
        if (file.isDirectory) {
            val newEntries = file.walkTopDown()
                    .filter { FileFilters.AUDIO.accept(it) }
                    .map { FileMetadata(it, metadataExtractor.extract(it)) }
                    .map { it.toQueueEntry(application) }
                    .toList()

            mediaQueue.push(QueueOp.AddToEnd(newEntries))
        } else {
            val metadata = folderContents.value.files.first { it.file == file }
            mediaQueue.push(QueueOp.AddToEnd(metadata.toQueueEntry(application)))
        }
    }

    data class Model(
            val folder: File,
            val files: List<FileMetadata>
    )

    data class FileMetadata(
            val file: File,
            val metadata: MetadataExtractor.Metadata?
    ) {

        val image by lazy {
            ArtworkRequestHandler.musicFileUri(file)
        }

        val title by lazy {
            FileUtils.getFileDisplayTitle(
                    FileUtils.getFileSortKey(file)?.substringBeforeLast(
                            "."
                    )
            )
        }

        private var description: CharSequence? = null

        fun description(context: Context): CharSequence? {
            if (description == null) {
                description = FileUtils.getFileDescription(context, metadata, true, true)
            }

            return description

        }

        fun toQueueEntry(context: Context) = QueueEntry(
                content = Uri.fromFile(file),
                image = image,
                metadata = QueueEntry.Metadata(
                        title = title,
                        description = description(context),
                        album = metadata?.album,
                        artist = metadata?.artist,
                        genre = metadata?.genre,
                        duration = metadata?.duration ?: -1L
                )
        )
    }
}
