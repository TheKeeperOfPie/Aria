package com.winsonchiu.aria.folders.folder

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.SimpleEpoxyController
import com.google.android.material.snackbar.Snackbar
import com.winsonchiu.aria.R
import com.winsonchiu.aria.folders.folder.view.FileItemView
import com.winsonchiu.aria.folders.root.FolderRootFragmentDaggerComponent
import com.winsonchiu.aria.framework.async.RequestState
import com.winsonchiu.aria.framework.fragment.FragmentInitializer
import com.winsonchiu.aria.framework.fragment.build
import com.winsonchiu.aria.framework.fragment.subclass.BaseFragment
import com.winsonchiu.aria.framework.menu.itemsheet.ItemsMenuDialogFragment
import com.winsonchiu.aria.framework.menu.itemsheet.ItemsMenuItem
import com.winsonchiu.aria.framework.menu.itemsheet.view.ItemsMenuFileHeaderView
import com.winsonchiu.aria.framework.menu.itemsheet.view.ItemsMenuIconWithTextView
import com.winsonchiu.aria.framework.util.dpToPx
import com.winsonchiu.aria.framework.util.setDataForView
import com.winsonchiu.aria.media.MediaBrowserConnection
import com.winsonchiu.aria.media.MediaQueue
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.folder_fragment.*
import java.io.File
import javax.inject.Inject

class FolderFragment : BaseFragment<FolderRootFragmentDaggerComponent, FolderFragmentDaggerComponent>(), ItemsMenuDialogFragment.Listener<FolderFragment.FolderItemOption> {

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
                view?.run {
                    mediaQueue.add(MediaQueue.QueueItem(context, fileMetadata))
                    Snackbar.make(this, getString(R.string.item_added, fileMetadata.file.name), Snackbar.LENGTH_SHORT)
                            .show()
                }
            }
        }

        override fun onLongClick(fileMetadata: FolderController.FileMetadata) {
//                folderController.addFolderToQueue(fileMetadata)
//            val item = MediaQueue.QueueItem(fileMetadata.file, fileMetadata.image, fileMetadata.metadata)
//            mediaQueue.add(item, item)
//            mediaBrowserConnection.mediaController.transportControls.play()

            val file = fileMetadata.file

            ItemsMenuDialogFragment.newInstance(
                    listOf(
                            FolderItemOption.Header(context!!, fileMetadata),
                            FolderItemOption.PlayNext(file),
                            FolderItemOption.AddToQueue(file)
                    )
            )
                    .show(childFragmentManager, null)
        }
    }

    private var epoxyController = SimpleEpoxyController()

    override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        folderSwipeRefresh.setOnRefreshListener { folderController.refresh() }
        folderSwipeRefresh.setDistanceToTriggerSync(150.dpToPx(view))

        folderRecyclerView.itemAnimator = FolderItemAnimator()
        folderRecyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
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

    override fun onClick(item: FolderItemOption) {
        when (item) {
            is FolderFragment.FolderItemOption.Header -> Unit
            is FolderFragment.FolderItemOption.PlayNext -> folderController.playNext(item.file)
            is FolderFragment.FolderItemOption.AddToQueue -> folderController.addToQueue(item.file)
        }.run { }
    }

    sealed class FolderItemOption : ItemsMenuItem {

        @Parcelize
        data class Header(
                override val data: ItemsMenuFileHeaderView.Model.Data
        ) : FolderItemOption(), ItemsMenuFileHeaderView.Model {
            constructor(
                    context: Context,
                    fileMetadata: FolderController.FileMetadata
            ) : this(
                    ItemsMenuFileHeaderView.Model.Data(
                            fileMetadata.title,
                            fileMetadata.description(context),
                            fileMetadata.image
                    )
            )
        }

        @Parcelize
        data class PlayNext(
                val file: File,
                override val data: ItemsMenuIconWithTextView.Model.Data = ItemsMenuIconWithTextView.Model.Data(
                        R.drawable.ic_queue_play_next_24dp,
                        R.string.play_next
                )
        ) : FolderItemOption(), ItemsMenuIconWithTextView.Model

        @Parcelize
        data class AddToQueue(
                val file: File,
                override val data: ItemsMenuIconWithTextView.Model.Data = ItemsMenuIconWithTextView.Model.Data(
                        R.drawable.ic_playlist_add_24dp,
                        R.string.add_to_queue
                )
        ) : FolderItemOption(), ItemsMenuIconWithTextView.Model
    }
}