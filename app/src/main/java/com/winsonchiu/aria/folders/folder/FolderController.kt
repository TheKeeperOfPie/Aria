package com.winsonchiu.aria.folders.folder

import android.annotation.SuppressLint
import android.os.Environment
import android.support.v4.app.Fragment
import com.jakewharton.rxrelay2.BehaviorRelay
import com.winsonchiu.aria.async.RequestState
import com.winsonchiu.aria.dagger.FragmentScreenScope
import com.winsonchiu.aria.dagger.fragment.FragmentLifecycleBoundComponent
import com.winsonchiu.aria.folders.util.FileFilters
import com.winsonchiu.aria.folders.util.FileSorter
import com.winsonchiu.aria.folders.util.withFolders
import com.winsonchiu.aria.music.MetadataExtractor
import com.winsonchiu.aria.music.artwork.ArtworkCache
import com.winsonchiu.aria.music.artwork.ArtworkExtractor
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@FragmentScreenScope
class FolderController @Inject constructor(
        private val metadataExtractor: MetadataExtractor,
        private val artworkExtractor: ArtworkExtractor,
        private val artworkCache: ArtworkCache
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

    private val files = folderFile.map {
        (it.listFiles(FileFilters.AUDIO.withFolders()) ?: emptyArray())
                .toList()
                .let { FileSorter.sort(it, FileSorter.Method.BY_NAME) }
                .map {
                    FileMetadata(
                            it,
                            null,
                            metadataExtractor.extract(it)
                    )
                }
    }.cache()

    private val filesWithArtwork = files.map {
        it.map { it.copy(image = artworkExtractor.getArtworkForFile(it.file, artworkCache)) }
    }

    private val filesWithArtworkAndDepthSearch = filesWithArtwork.map {
        it.map {
            it.copy(
                    image = artworkExtractor.getArtworkWithFileDepthSearch(
                            it.file,
                            artworkCache
                    )
            )
        }
    }

    init {
        stateChange.debounce(500, TimeUnit.MILLISECONDS)
                .subscribe(state)
    }

    @SuppressLint("CheckResult")
    override fun onFirstInitialize(fragment: Fragment) {
        super.onFirstInitialize(fragment)
        files.mergeWith(filesWithArtwork)
                .mergeWith(filesWithArtworkAndDepthSearch)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { stateChange.accept(RequestState.LOADING) }
                .doFinally { stateChange.accept(RequestState.DONE) }
                .withLatestFrom(folderFile.toFlowable(), BiFunction<List<FileMetadata>, File, Model> { it, folderFile ->
                    Model(folderFile, it)
                })
                .bindToLifecycle()
                .subscribe(folderContents)
    }

    @SuppressLint("CheckResult")
    fun refresh() {
        filesWithArtwork.mergeWith(filesWithArtworkAndDepthSearch)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { stateChange.accept(RequestState.LOADING) }
                .doFinally { stateChange.accept(RequestState.DONE) }
                .withLatestFrom(folderFile.toFlowable(), BiFunction<List<FileMetadata>, File, Model> { it, folderFile ->
                    Model(folderFile, it)
                })
                .bindToLifecycle()
                .subscribe(folderContents)
    }

    data class Model(
            val folder: File,
            val files: List<FileMetadata>
    )

    data class FileMetadata(
            val file: File,
            val image: ArtworkCache.Metadata?,
            val metadata: MetadataExtractor.Metadata?
    )
}