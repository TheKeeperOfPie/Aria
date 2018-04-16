package com.winsonchiu.aria.folders.folder

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.airbnb.epoxy.SimpleEpoxyController
import com.winsonchiu.aria.R
import com.winsonchiu.aria.async.RequestState
import com.winsonchiu.aria.folders.root.FolderRootFragmentDaggerComponent
import com.winsonchiu.aria.fragment.FragmentInitializer
import com.winsonchiu.aria.fragment.arg
import com.winsonchiu.aria.fragment.build
import com.winsonchiu.aria.fragment.subclass.BaseFragment
import com.winsonchiu.aria.util.animation.transition.DoNothingAsOverlay
import com.winsonchiu.aria.util.animation.transition.GhostViewOverlay
import com.winsonchiu.aria.util.animation.transition.SlideAndFade
import com.winsonchiu.aria.util.dpToPx
import com.winsonchiu.aria.util.setDataForView
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.folder_fragment.folderRecyclerView
import kotlinx.android.synthetic.main.folder_fragment.folderSwipeRefresh
import kotlinx.android.synthetic.main.folder_fragment.folderTitleText
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FolderFragment : BaseFragment<FolderRootFragmentDaggerComponent, FolderFragmentDaggerComponent>() {

    override fun makeComponent(parentComponent: FolderRootFragmentDaggerComponent): FolderFragmentDaggerComponent {
        return parentComponent.folderFragmentComponent()
    }

    override fun injectSelf(component: FolderFragmentDaggerComponent) = component.inject(this)

    object Args : Builder()
    open class Builder : FragmentInitializer<FolderFragment>({
        FolderFragment().apply {
            enterTransition = SlideAndFade(
                    slideFractionFromY = 0.5f,
                    slideFractionToY = 0f,
                    fadeFrom = 0f,
                    fadeTo = 1f,
                    overlayMode = GhostViewOverlay.OverlayMode.GHOST
            ).forSupport()
            returnTransition = SlideAndFade(
                    slideFractionFromY = 0f,
                    slideFractionToY = 0.5f,
                    fadeFrom = 1f,
                    fadeTo = 0f,
                    overlayMode = GhostViewOverlay.OverlayMode.FRAMEWORK_VISIBILITY
            ).forSupport()
            exitTransition = DoNothingAsOverlay.forSupport()
            reenterTransition = DoNothingAsOverlay.forSupport()
        }
    }) {
        val folder = string("folder")
    }

    override val layoutId = R.layout.folder_fragment

    val folder = arg(Args.folder)

    @Inject
    lateinit var folderController: FolderController

    private var epoxyController = SimpleEpoxyController()

    private val listener = object : FileItemView.Listener {
        override fun onClick(file: File) {
            if (file.isDirectory) {
                fragmentManager?.run {
                    val newFragment = Builder().build { folder put file.absolutePath }
                    exitTransition = DoNothingAsOverlay.forSupport()
                    reenterTransition = DoNothingAsOverlay.forSupport()

                    beginTransaction().setReorderingAllowed(true)
                            .replace(id, newFragment)
                            .addToBackStack(null)
                            .commit()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        folderSwipeRefresh.setOnRefreshListener { folderController.refresh() }
        folderSwipeRefresh.setDistanceToTriggerSync(150.dpToPx(view))

        folderRecyclerView.itemAnimator = FolderItemAnimator()
        folderRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        folderRecyclerView.setHasFixedSize(true)

        postponeEnterTransition(150)
    }

    override fun onStart() {
        super.onStart()

        folderController.state
                .startWith(RequestState.NONE)
                .debounce(500, TimeUnit.MILLISECONDS)
                .map {
                    when (it) {
                        RequestState.NONE, RequestState.DONE -> false
                        RequestState.LOADING -> true
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle()
                .subscribe { folderSwipeRefresh.isRefreshing = it }

        folderController.folderContents
                .map { FolderViewModelTransformer.transform(context!!, listener, it) }
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle()
                .subscribe {
                    folderTitleText.text = it.folderTitle
                    epoxyController.setDataForView(folderRecyclerView, it.models)
                }
    }
}