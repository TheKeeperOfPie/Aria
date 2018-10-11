package com.winsonchiu.aria.source.artists

import android.app.Application
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import com.jakewharton.rxrelay2.BehaviorRelay
import com.winsonchiu.aria.artwork.ArtworkRequestHandler
import com.winsonchiu.aria.framework.async.RequestState
import com.winsonchiu.aria.framework.dagger.fragment.FragmentLifecycleBoundComponent
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@ArtistsFragmentScreenScope
class ArtistsController @Inject constructor(
        private val application: Application
) : FragmentLifecycleBoundComponent() {

    companion object {
        private val PROJECTION = arrayOf(
                MediaStore.Audio.Artists._ID,
                MediaStore.Audio.ArtistColumns.ARTIST,
                MediaStore.Audio.ArtistColumns.ARTIST_KEY,
                MediaStore.Audio.ArtistColumns.NUMBER_OF_ALBUMS,
                MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS
        )
    }

    val model = BehaviorRelay.create<Model>()
    val state = BehaviorRelay.createDefault<RequestState>(RequestState.NONE)

    private val stateChange = BehaviorRelay.createDefault<RequestState>(RequestState.NONE)

    private val refreshRelay = BehaviorRelay.createDefault(System.currentTimeMillis())

    fun refresh() = refreshRelay.accept(System.currentTimeMillis())

    override fun onFirstInitialize(fragment: Fragment) {
        super.onFirstInitialize(fragment)

        stateChange.debounce(500, TimeUnit.MILLISECONDS)
                .bindToLifecycle()
                .subscribe(state)

        refreshRelay.switchMapSingle {
            Single.fromCallable {
                application.contentResolver.query(
                        MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                        PROJECTION,
                        null,
                        null,
                        "${MediaStore.Audio.ArtistColumns.ARTIST_KEY} ASC"
                )?.use {
                    val artists = mutableListOf<Artist>()

                    if (it.moveToFirst()) {
                        val idIndex = it.getColumnIndex(MediaStore.Audio.Artists._ID)
                        val nameIndex = it.getColumnIndex(MediaStore.Audio.ArtistColumns.ARTIST)
                        val keyIndex = it.getColumnIndex(MediaStore.Audio.ArtistColumns.ARTIST_KEY)
                        val albumCountIndex = it.getColumnIndex(MediaStore.Audio.ArtistColumns.NUMBER_OF_ALBUMS)
                        val trackCountIndex = it.getColumnIndex(MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS)

                        do {
                            val artistId = it.getString(idIndex)
                            artists += Artist(
                                    id = ArtistId(artistId),
                                    name = it.getString(nameIndex),
                                    image = ArtworkRequestHandler.artistUri(artistId),
                                    key = ArtistKey(it.getString(keyIndex)),
                                    albumCount = it.getInt(albumCountIndex),
                                    trackCount = it.getInt(trackCountIndex)
                            )
                        } while (it.moveToNext())
                    }

                    return@use artists as List<Artist>
                } ?: emptyList()
            }
                    .doOnSubscribe { stateChange.accept(RequestState.LOADING) }
                    .doFinally { stateChange.accept(RequestState.DONE) }
        }
                .map(::Model)
                .subscribeOn(Schedulers.io())
                .bindToLifecycle()
                .subscribe(model)
    }

    data class Model(
            val artists: List<Artist>
    )
}