package com.winsonchiu.aria.folder

import android.annotation.SuppressLint
import android.os.Environment
import com.jakewharton.rxrelay2.BehaviorRelay
import com.winsonchiu.aria.async.RequestState
import com.winsonchiu.aria.dagger.FragmentScreenScope
import com.winsonchiu.aria.dagger.fragment.FragmentLifecycleBoundComponent
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.File

@FragmentScreenScope
class FolderController : FragmentLifecycleBoundComponent() {

    private val folder by arg(FolderFragment.Args.folder)

    val folderContents = BehaviorRelay.create<List<File>>()
    val state = BehaviorRelay.createDefault<RequestState>(RequestState.NONE)

    override fun onFirstInitialize() {
        super.onFirstInitialize()

        refresh()
    }

    @SuppressLint("CheckResult")
    fun refresh() {
        Single.fromCallable {
            if (folder.isNullOrBlank()) {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            } else {
                File(folder)
            }
        }
                .map { it.listFiles().toList() }
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { state.accept(RequestState.LOADING) }
                .doFinally { state.accept(RequestState.DONE) }
                .subscribe(folderContents)
    }
}