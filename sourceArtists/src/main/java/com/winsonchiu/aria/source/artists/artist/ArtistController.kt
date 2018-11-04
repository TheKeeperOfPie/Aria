package com.winsonchiu.aria.source.artists.artist

import android.app.Application
import androidx.fragment.app.Fragment
import com.jakewharton.rxrelay2.BehaviorRelay
import com.winsonchiu.aria.framework.dagger.fragment.FragmentLifecycleBoundComponent
import com.winsonchiu.aria.queue.MediaQueue
import com.winsonchiu.aria.queue.QueueOp
import com.winsonchiu.aria.source.artists.ArtistId
import com.winsonchiu.aria.source.artists.ArtistsUtils
import com.winsonchiu.aria.source.artists.artist.media.ArtistMedia
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@ArtistFragmentScreenScope
class ArtistController @Inject constructor(
        private val application: Application,
        private val mediaQueue: MediaQueue
) : FragmentLifecycleBoundComponent() {

    val model = BehaviorRelay.create<Model>()

    private val artistId by arg(ArtistFragment.Args.artistId)

    private val refreshRelay = BehaviorRelay.createDefault(System.currentTimeMillis())

    override fun onFirstInitialize(fragment: Fragment) {
        super.onFirstInitialize(fragment)

        refreshRelay
                .switchMapSingle {
                    Single.fromCallable {
                        ArtistsUtils.readMedia(application, ArtistId(artistId))
                    }
                            .subscribeOn(Schedulers.io())
                            .map { Result.success(it) }
                            .onErrorReturn { Result.failure(it) }
                }
                .map {
                    Model(
                            it.getOrDefault(emptyList()),
                            it.exceptionOrNull()
                    )
                }
                .bindToLifecycle()
                .subscribe(model)
    }

    fun refresh() {
        refreshRelay.accept(System.currentTimeMillis())
    }

    fun playNext(media: ArtistMedia) {
        mediaQueue.push(QueueOp.AddNext(media.toQueueEntry(application)))
    }

    fun addToQueue(media: ArtistMedia) {
        mediaQueue.push(QueueOp.AddToEnd(media.toQueueEntry(application)))
    }

    data class Model(
            val artistMedia: List<ArtistMedia>,
            val throwable: Throwable?
    )

}