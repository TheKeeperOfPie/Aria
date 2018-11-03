package com.winsonchiu.aria.source.artists.artist

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.SimpleEpoxyController
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.winsonchiu.aria.framework.fragment.FragmentInitializer
import com.winsonchiu.aria.framework.fragment.arg
import com.winsonchiu.aria.framework.fragment.subclass.BaseFragment
import com.winsonchiu.aria.framework.util.setDataForView
import com.winsonchiu.aria.itemsheet.ItemsMenuDialogFragment
import com.winsonchiu.aria.itemsheet.ItemsMenuItem
import com.winsonchiu.aria.itemsheet.view.ItemsMenuFileHeaderView
import com.winsonchiu.aria.itemsheet.view.ItemsMenuIconWithTextView
import com.winsonchiu.aria.queue.MediaQueue
import com.winsonchiu.aria.source.artists.ArtistId
import com.winsonchiu.aria.source.artists.ArtistsRootFragmentDaggerComponent
import com.winsonchiu.aria.source.artists.R
import com.winsonchiu.aria.source.artists.artist.media.ArtistMedia
import com.winsonchiu.aria.source.artists.artist.media.ArtistMediaItemView
import com.winsonchiu.aria.source.artists.artist.media.ArtistMediaItemViewModel_
import com.winsonchiu.aria.source.artists.transition.ArtistsToArtistTransition
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.artist_fragment.*
import javax.inject.Inject

class ArtistFragment : BaseFragment<ArtistsRootFragmentDaggerComponent, ArtistFragmentDaggerComponent>() {

    companion object {

        private const val ITEM_OPTION_REQUEST_CODE = 12352
    }

    override val layoutId = R.layout.artist_fragment

    override fun makeComponent(parentComponent: ArtistsRootFragmentDaggerComponent) = parentComponent.artistFragmentComponent()

    override fun injectSelf(component: ArtistFragmentDaggerComponent) = component.inject(this)

    object Args : Builder()
    open class Builder : FragmentInitializer<ArtistFragment>({ ArtistFragment() }) {
        val artistId = string<ArtistId, String>({ it.value }, "artistId") {
            it
        }
        val artistName = string("artistName")
        val imageUri = parcelable<Uri?>("imageUri")
    }

    @Inject
    lateinit var controller: ArtistController

    @Inject
    lateinit var queue: MediaQueue

    private val artistId by arg(Args.artistId)
    private val artistName by arg(Args.artistName)
    private val imageUri by arg(Args.imageUri)

    private val epoxyController = SimpleEpoxyController()

    private val imageCallback = object : Callback {
        override fun onSuccess() {
            view?.postOnAnimation {
                startPostponedEnterTransition()
            }
        }

        override fun onError(e: Exception?) {
            startPostponedEnterTransition()
        }
    }

    private val listener = object : ArtistMediaItemView.Listener {
        override fun onClick(data: ArtistMedia) {
            controller.playNext(data)
        }

        override fun onLongClick(data: ArtistMedia) {
            ItemsMenuDialogFragment.newInstance(
                    listOf(
                            MediaItemOption.Header(data),
                            MediaItemOption.PlayNext(data),
                            MediaItemOption.AddToQueue(data)
                    )
            )
                    .show(this@ArtistFragment, ITEM_OPTION_REQUEST_CODE)
        }
    }

    override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        postponeEnterTransition(300)

        layoutCollapsingToolbar.title = artistName
        layoutCollapsingToolbar.setContentScrimColor(Color.BLACK)

        layoutAppBar.transitionName = ArtistsToArtistTransition.header(ArtistId(artistId))
        imageArtistLayout.clipToOutline = true
        Picasso.get()
                .load(imageUri)
                .resize(resources.displayMetrics.widthPixels, 0)
                .onlyScaleDown()
                .into(imageArtist, imageCallback)

        recyclerMedia.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
    }

    override fun onStart() {
        super.onStart()
        controller.model
                .observeOn(Schedulers.computation())
                .map {
                    it.artistMedia.map {
                        ArtistMediaItemViewModel_()
                                .id(it.id)
                                .data(it)
                                .listener(listener)
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle()
                .subscribe {
                    epoxyController.setDataForView(recyclerMedia, it)
                }
    }

    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?
    ) {
        if (requestCode == ITEM_OPTION_REQUEST_CODE) {
            data?.setExtrasClassLoader(ArtistFragment::class.java.classLoader)
            val item = data?.getParcelableExtra<MediaItemOption>(ItemsMenuDialogFragment.KEY_RESULT_ITEM)

            when (item) {
                is MediaItemOption.Header -> Unit
                is MediaItemOption.PlayNext -> controller.playNext(item.media)
                is MediaItemOption.AddToQueue -> controller.addToQueue(item.media)
                null -> {}
            }.run { }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private sealed class MediaItemOption : ItemsMenuItem {

        @Parcelize
        data class Header(
                override val data: ItemsMenuFileHeaderView.Model.Data
        ) : MediaItemOption(), ItemsMenuFileHeaderView.Model {
            constructor(
                    media: ArtistMedia
            ) : this(
                    ItemsMenuFileHeaderView.Model.Data(
                            media.title,
                            media.description,
                            media.image
                    )
            )
        }

        @Parcelize
        data class PlayNext(
                val media: ArtistMedia,
                override val data: ItemsMenuIconWithTextView.Model.Data = ItemsMenuIconWithTextView.Model.Data(
                        R.drawable.ic_queue_play_next_24dp,
                        R.string.play_next
                )
        ) : MediaItemOption(), ItemsMenuIconWithTextView.Model

        @Parcelize
        data class AddToQueue(
                val media: ArtistMedia,
                override val data: ItemsMenuIconWithTextView.Model.Data = ItemsMenuIconWithTextView.Model.Data(
                        R.drawable.ic_playlist_add_24dp,
                        R.string.add_to_queue
                )
        ) : MediaItemOption(), ItemsMenuIconWithTextView.Model
    }
}