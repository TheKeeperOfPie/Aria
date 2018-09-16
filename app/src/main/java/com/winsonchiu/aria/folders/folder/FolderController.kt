package com.winsonchiu.aria.folders.folder

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Environment
import androidx.fragment.app.Fragment
import com.jakewharton.rxrelay2.BehaviorRelay
import com.winsonchiu.aria.folders.util.FileFilters
import com.winsonchiu.aria.folders.util.FileSorter
import com.winsonchiu.aria.folders.util.FileUtils
import com.winsonchiu.aria.folders.util.withFolders
import com.winsonchiu.aria.framework.async.RequestState
import com.winsonchiu.aria.framework.dagger.FragmentScreenScope
import com.winsonchiu.aria.framework.dagger.fragment.FragmentLifecycleBoundComponent
import com.winsonchiu.aria.media.MediaQueue
import com.winsonchiu.aria.music.MetadataExtractor
import com.winsonchiu.aria.music.artwork.ArtworkRequestHandler
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@FragmentScreenScope
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

    fun addFolderToQueue(selected: FileMetadata) {
        folderContents.value.files
                .map { MediaQueue.QueueItem(application, it) }
                .also { mediaQueue.add(it, MediaQueue.QueueItem(application, selected)) }
    }

    fun playNext(file: File) {
        val metadata = folderContents.value.files.first { it.file == file }
        mediaQueue.playNext(MediaQueue.QueueItem(application, metadata))
    }

    fun addToQueue(file: File) {
        val metadata = folderContents.value.files.first { it.file == file }
        mediaQueue.add(MediaQueue.QueueItem(application, metadata))
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
    }
}