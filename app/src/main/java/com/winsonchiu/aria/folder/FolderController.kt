package com.winsonchiu.aria.folder

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Environment
import android.support.v4.app.Fragment
import com.jakewharton.rxrelay2.BehaviorRelay
import com.winsonchiu.aria.async.RequestState
import com.winsonchiu.aria.dagger.FragmentScreenScope
import com.winsonchiu.aria.dagger.fragment.FragmentLifecycleBoundComponent
import com.winsonchiu.aria.folder.util.FileFilters
import com.winsonchiu.aria.folder.util.FileSorter
import com.winsonchiu.aria.folder.util.withFolders
import com.winsonchiu.aria.music.artwork.ArtworkCache
import com.winsonchiu.aria.music.artwork.ArtworkExtractor
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.File
import javax.inject.Inject

@FragmentScreenScope
class FolderController @Inject constructor(
        private val artworkExtractor: ArtworkExtractor,
        private val artworkCache: ArtworkCache
) : FragmentLifecycleBoundComponent() {

    val folderContents = BehaviorRelay.create<List<FileModel>>()
    val state = BehaviorRelay.createDefault<RequestState>(RequestState.NONE)

    private val folder by arg(FolderFragment.Args.folder)

    private val files = Single.fromCallable {
        if (folder.isNullOrBlank()) {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        } else {
            File(folder)
        }
    }
            .map {
                it.listFiles(FileFilters.AUDIO.withFolders())
                        .toList()
                        .let { FileSorter.sort(it, FileSorter.Method.BY_NAME) }
                        .map { FileModel(it, null) }
            }
            .cache()

    private val filesWithArtwork = files.map { it.map { it.copy(image = artworkExtractor.getArtwork(it.file, artworkCache)) } }

    private val filesWithArtworkAndDepthSearch = filesWithArtwork.map { it.map { it.copy(image = artworkExtractor.getArtworkWithFileDepthSearch(it.file, artworkCache)) } }

    @SuppressLint("CheckResult")
    override fun onFirstInitialize(fragment: Fragment) {
        super.onFirstInitialize(fragment)
        files.mergeWith(filesWithArtwork)
                .mergeWith(filesWithArtworkAndDepthSearch)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { state.accept(RequestState.LOADING) }
                .doFinally { state.accept(RequestState.DONE) }
                .subscribe(folderContents)
    }

    @SuppressLint("CheckResult")
    fun refresh() {
        filesWithArtwork.mergeWith(filesWithArtworkAndDepthSearch)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { state.accept(RequestState.LOADING) }
                .doFinally { state.accept(RequestState.DONE) }
                .subscribe(folderContents)
    }

    data class FileModel(
            val file: File,
            val image: Bitmap?
    )
}