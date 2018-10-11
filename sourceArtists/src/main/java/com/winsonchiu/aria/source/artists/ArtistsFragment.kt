package com.winsonchiu.aria.source.artists

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.SimpleEpoxyController
import com.winsonchiu.aria.framework.async.RequestState
import com.winsonchiu.aria.framework.fragment.subclass.BaseFragment
import com.winsonchiu.aria.framework.util.dpToPx
import com.winsonchiu.aria.framework.util.setDataForView
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.artists_fragment.*
import javax.inject.Inject

class ArtistsFragment : BaseFragment<ArtistsFragmentDaggerComponent.ComponentProvider, ArtistsFragmentDaggerComponent>() {

    override val layoutId = R.layout.artists_fragment

    override fun makeComponent(parentComponent: ArtistsFragmentDaggerComponent.ComponentProvider) = parentComponent.artistsFragmentComponent()

    override fun injectSelf(component: ArtistsFragmentDaggerComponent) = component.inject(this)

    @Inject
    lateinit var artistsController: ArtistsController

    private lateinit var epoxyController: SimpleEpoxyController

    private lateinit var layoutManager: GridLayoutManager

    private val listener = object : ArtistItemView.Listener {
        override fun onClick(artist: Artist) {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
                .map { ArtistsViewModelTransformer.transform(context!!, listener, it) }
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle()
                .subscribe {
                    epoxyController.setDataForView(artistsRecyclerView, it)
                }
    }
}