package com.winsonchiu.aria.folders.folder

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.airbnb.epoxy.SimpleEpoxyController
import com.winsonchiu.aria.R
import com.winsonchiu.aria.folders.root.FolderRootFragmentDaggerComponent
import com.winsonchiu.aria.framework.async.RequestState
import com.winsonchiu.aria.framework.fragment.FragmentInitializer
import com.winsonchiu.aria.framework.fragment.build
import com.winsonchiu.aria.framework.fragment.subclass.BaseFragment
import com.winsonchiu.aria.framework.util.dpToPx
import com.winsonchiu.aria.framework.util.setDataForView
import com.winsonchiu.aria.media.MediaBrowserConnection
import com.winsonchiu.aria.media.MediaQueue
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.folder_fragment.*
import javax.inject.Inject

class FolderFragment : BaseFragment<FolderRootFragmentDaggerComponent, FolderFragmentDaggerComponent>() {

    override fun makeComponent(parentComponent: FolderRootFragmentDaggerComponent): FolderFragmentDaggerComponent {
        return parentComponent.folderFragmentComponent()
    }

    override fun injectSelf(component: FolderFragmentDaggerComponent) = component.inject(this)

    object Args : Builder()
    open class Builder : FragmentInitializer<FolderFragment>({ FolderFragment() }) {
        val folder = string("folder")
    }

    override val layoutId = R.layout.folder_fragment

    @Inject
    lateinit var folderController: FolderController

    @Inject
    lateinit var mediaBrowserConnection: MediaBrowserConnection

    @Inject
    lateinit var mediaQueue: MediaQueue

    private val listener = object : FileItemView.Listener {
        override fun onClick(fileMetadata: FolderController.FileMetadata) {
            val file = fileMetadata.file
            if (file.isDirectory) {
                fragmentManager?.run {
                    val newFragment = Builder().build { folder put file.absolutePath }
                    FolderToFolderTransition.applyToFragment(newFragment)
                    FolderToFolderTransition.applyToFragment(this@FolderFragment)

                    beginTransaction().setReorderingAllowed(true)
                            .replace(id, newFragment)
                            .addToBackStack(null)
                            .commit()
                }
            } else {
                mediaQueue.add(MediaQueue.QueueItem(fileMetadata.file, fileMetadata.image, fileMetadata.metadata))
                view?.run {
                    Snackbar.make(this, getString(R.string.item_added, fileMetadata.file.name), Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        override fun onLongClick(fileMetadata: FolderController.FileMetadata) {
//                folderController.addFolderToQueue(fileMetadata)
            val item = MediaQueue.QueueItem(fileMetadata.file, fileMetadata.image, fileMetadata.metadata)
            mediaQueue.add(item, item)
            mediaBrowserConnection.mediaController.transportControls.play()
        }
    }

    private var epoxyController = SimpleEpoxyController()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        folderSwipeRefresh.setOnRefreshListener { folderController.refresh() }
        folderSwipeRefresh.setDistanceToTriggerSync(150.dpToPx(view))

        folderRecyclerView.itemAnimator = FolderItemAnimator()
        folderRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        folderRecyclerView.setHasFixedSize(true)

        postponeEnterTransition(150)

        val folderSwipeRefresh = folderSwipeRefresh
        val folderTitleText = folderTitleText.apply { transitionName = uniqueTransitionName + id }
        val folderRecyclerView = folderRecyclerView.apply { transitionName = uniqueTransitionName + id }

        folderController.state
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