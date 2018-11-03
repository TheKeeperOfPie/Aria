package com.winsonchiu.aria.source.artists.artists

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.SimpleEpoxyController
import com.winsonchiu.aria.framework.async.RequestState
import com.winsonchiu.aria.framework.fragment.build
import com.winsonchiu.aria.framework.fragment.subclass.BaseFragment
import com.winsonchiu.aria.framework.util.dpToPx
import com.winsonchiu.aria.framework.util.setDataForView
import com.winsonchiu.aria.framework.view.recyclerview.GridSpacingItemDecoration
import com.winsonchiu.aria.itemsheet.ItemsMenuDialogFragment
import com.winsonchiu.aria.itemsheet.ItemsMenuItem
import com.winsonchiu.aria.itemsheet.view.ItemsMenuFileHeaderView
import com.winsonchiu.aria.itemsheet.view.ItemsMenuIconWithTextView
import com.winsonchiu.aria.source.artists.Artist
import com.winsonchiu.aria.source.artists.ArtistsRootFragmentDaggerComponent
import com.winsonchiu.aria.source.artists.ArtistsUtils
import com.winsonchiu.aria.source.artists.R
import com.winsonchiu.aria.source.artists.artist.ArtistFragment
import com.winsonchiu.aria.source.artists.transition.ArtistsToArtistTransition
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.artists_fragment.*
import javax.inject.Inject

class ArtistsFragment : BaseFragment<ArtistsRootFragmentDaggerComponent, ArtistsFragmentDaggerComponent>() {

    companion object {

        private const val ITEM_OPTION_REQUEST_CODE = 11512
    }

    override val layoutId = R.layout.artists_fragment

    override fun makeComponent(parentComponent: ArtistsRootFragmentDaggerComponent) = parentComponent.artistsFragmentComponent()

    override fun injectSelf(component: ArtistsFragmentDaggerComponent) = component.inject(this)

    @Inject
    lateinit var controller: ArtistsController

    private lateinit var epoxyController: SimpleEpoxyController

    private lateinit var layoutManager: GridLayoutManager

    private val listener = object : ArtistItemView.Listener {
        override fun onClick(view: ArtistItemView, artist: Artist) {
            fragmentManager?.run {
                val fragment = ArtistFragment.Builder().build {
                    artistId put artist.id
                    artistName put artist.name
                    imageUri put artist.image
                }

                ArtistsToArtistTransition
                        .applyTo(view, this@ArtistsFragment, fragment)

                beginTransaction()
                        .setReorderingAllowed(true)
                        .addSharedElement(view, view.transitionName)
                        .replace(R.id.artistsRootFragmentContainer, fragment)
                        .addToBackStack(null)
                        .commit()
            }
        }

        override fun onLongClick(artist: Artist) {
            ItemsMenuDialogFragment.newInstance(
                    listOf(
                            ArtistItemOption.Header(artist),
                            ArtistItemOption.PlayNext(artist),
                            ArtistItemOption.AddToQueue(artist)
                    )
            )
                    .show(this@ArtistsFragment, ITEM_OPTION_REQUEST_CODE)
        }
    }

    override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        artistsSwipeRefresh.setOnRefreshListener { controller.refresh() }
        artistsSwipeRefresh.setDistanceToTriggerSync(150.dpToPx(view))

        val spanCount = ArtistsUtils.spanCount(view.context)

        epoxyController = SimpleEpoxyController()
        epoxyController.spanCount = spanCount

        layoutManager = GridLayoutManager(context, spanCount, RecyclerView.VERTICAL, false)
        layoutManager.spanSizeLookup = epoxyController.spanSizeLookup

        artistsRecyclerView.layoutManager = layoutManager
        artistsRecyclerView.setHasFixedSize(true)
        artistsRecyclerView.addItemDecoration(GridSpacingItemDecoration(12.dpToPx(view), true))

        postponeEnterTransition(150)

        controller.state
                .map {
                    when (it) {
                        RequestState.NONE, RequestState.DONE -> false
                        RequestState.LOADING -> true
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle()
                .subscribe { artistsSwipeRefresh.isRefreshing = it }

        controller.model
                .map {
                    ArtistsViewModelTransformer
                            .transform(context!!, listener, it)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle()
                .subscribe {
                    epoxyController.setDataForView(artistsRecyclerView, it)
                }
    }

    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?
    ) {
        if (requestCode == ITEM_OPTION_REQUEST_CODE) {
            data?.setExtrasClassLoader(ArtistFragment::class.java.classLoader)
            val item = data?.getParcelableExtra<ArtistItemOption>(ItemsMenuDialogFragment.KEY_RESULT_ITEM)

            when (item) {
                is ArtistItemOption.Header -> Unit
                is ArtistItemOption.PlayNext -> controller.playNext(item.artist)
                is ArtistItemOption.AddToQueue -> controller.addToQueue(item.artist)
                null -> {}
            }.run { }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    sealed class ArtistItemOption : ItemsMenuItem {

        @Parcelize
        data class Header(
                override val data: ItemsMenuFileHeaderView.Model.Data
        ) : ArtistItemOption(), ItemsMenuFileHeaderView.Model {
            constructor(
                    artist: Artist
            ) : this(
                    ItemsMenuFileHeaderView.Model.Data(
                            artist.name,
                            null,
                            artist.image
                    )
            )
        }

        @Parcelize
        data class PlayNext(
                val artist: Artist,
                override val data: ItemsMenuIconWithTextView.Model.Data = ItemsMenuIconWithTextView.Model.Data(
                        R.drawable.ic_queue_play_next_24dp,
                        R.string.play_next
                )
        ) : ArtistItemOption(), ItemsMenuIconWithTextView.Model

        @Parcelize
        data class AddToQueue(
                val artist: Artist,
                override val data: ItemsMenuIconWithTextView.Model.Data = ItemsMenuIconWithTextView.Model.Data(
                        R.drawable.ic_playlist_add_24dp,
                        R.string.add_to_queue
                )
        ) : ArtistItemOption(), ItemsMenuIconWithTextView.Model
    }
}