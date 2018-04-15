package com.winsonchiu.aria.folder

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import androidx.core.view.postDelayed
import com.winsonchiu.aria.R
import com.winsonchiu.aria.async.RequestState
import com.winsonchiu.aria.dagger.ActivityComponent
import com.winsonchiu.aria.folder.root.FolderRootFragmentDaggerComponent
import com.winsonchiu.aria.fragment.BaseFragment
import com.winsonchiu.aria.fragment.FragmentInitializer
import com.winsonchiu.aria.fragment.arg
import com.winsonchiu.aria.fragment.build
import com.winsonchiu.aria.util.animation.transition.DoNothingAsOverlay
import com.winsonchiu.aria.util.animation.transition.GhostViewOverlay
import com.winsonchiu.aria.util.animation.transition.SlideAndFade
import com.winsonchiu.aria.util.setDataForView
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.folder_fragment.folderRecyclerView
import kotlinx.android.synthetic.main.folder_fragment.folderSwipeRefresh
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FolderFragment : BaseFragment<FolderFragmentDaggerComponent>() {

    @Suppress("UNCHECKED_CAST")
    override fun makeComponent(activityComponent: ActivityComponent): FolderFragmentDaggerComponent {
        return (parentFragment as BaseFragment<FolderRootFragmentDaggerComponent>).loader.fragmentComponent!!.folderFragmentComponent()
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

    private var epoxyController = FolderEpoxyController()

    private val listener = object : FolderFileItemView.Listener {
        override fun onClick(fileModel: FolderController.FileModel) {
            val (file, _) = fileModel
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

    override fun toString() = "${super.toString()} ${folder.value}"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        folderSwipeRefresh.setOnRefreshListener { folderController.refresh() }

        folderRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        folderRecyclerView.setHasFixedSize(true)

        postponeEnterTransition()

        view.postDelayed(150) {
            startPostponedEnterTransition()
        }
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
                .map {
                    it.map {
                        FolderFileItemViewModel_()
                                .id(it.file.name)
                                .fileModel(it)
                                .listener(listener)
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle()
                .subscribe { epoxyController.setDataForView(folderRecyclerView, FolderEpoxyController.Model(it)) }
    }
}