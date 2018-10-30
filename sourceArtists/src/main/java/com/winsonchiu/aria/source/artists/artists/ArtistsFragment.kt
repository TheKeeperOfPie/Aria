package com.winsonchiu.aria.source.artists.artists

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
import com.winsonchiu.aria.source.artists.Artist
import com.winsonchiu.aria.source.artists.ArtistsRootFragmentDaggerComponent
import com.winsonchiu.aria.source.artists.transition.ArtistsToArtistTransition
import com.winsonchiu.aria.source.artists.ArtistsUtils
import com.winsonchiu.aria.source.artists.R
import com.winsonchiu.aria.source.artists.artist.ArtistFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.artists_fragment.*
import javax.inject.Inject

class ArtistsFragment : BaseFragment<ArtistsRootFragmentDaggerComponent, ArtistsFragmentDaggerComponent>() {

    override val layoutId = R.layout.artists_fragment

    override fun makeComponent(parentComponent: ArtistsRootFragmentDaggerComponent) = parentComponent.artistsFragmentComponent()

    override fun injectSelf(component: ArtistsFragmentDaggerComponent) = component.inject(this)

    @Inject
    lateinit var artistsController: ArtistsController

    private lateinit var epoxyController: SimpleEpoxyController

    private lateinit var layoutManager: GridLayoutManager

    private val listener = object : ArtistItemView.Listener {
        override fun onClick(view: ArtistItemView, artist: Artist) {
            fragmentManager?.run {
                val fragment = ArtistFragment.Builder().build {
                    artistId put artist.id
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
    }

    override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        artistsSwipeRefresh.setOnRefreshListener { artistsController.refresh() }
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

        artistsController.state
                .map {
                    when (it) {
                        RequestState.NONE, RequestState.DONE -> false
                        RequestState.LOADING -> true
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle()
                .subscribe { artistsSwipeRefresh.isRefreshing = it }

        artistsController.model
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
}