package com.winsonchiu.aria.source.folders.inner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.SimpleEpoxyController
import com.winsonchiu.aria.framework.async.RequestState
import com.winsonchiu.aria.framework.fragment.FragmentInitializer
import com.winsonchiu.aria.framework.fragment.build
import com.winsonchiu.aria.framework.fragment.subclass.BaseFragment
import com.winsonchiu.aria.framework.util.dpToPx
import com.winsonchiu.aria.framework.util.setDataForView
import com.winsonchiu.aria.itemsheet.ItemsMenuDialogFragment
import com.winsonchiu.aria.itemsheet.ItemsMenuItem
import com.winsonchiu.aria.itemsheet.view.ItemsMenuFileHeaderView
import com.winsonchiu.aria.itemsheet.view.ItemsMenuIconWithTextView
import com.winsonchiu.aria.source.folders.FileEntry
import com.winsonchiu.aria.source.folders.R
import com.winsonchiu.aria.source.folders.inner.view.FileItemView
import com.winsonchiu.aria.source.folders.root.FolderRootFragmentDaggerComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.folder_fragment.*
import javax.inject.Inject

class FolderFragment : BaseFragment<FolderRootFragmentDaggerComponent, FolderFragmentDaggerComponent>()  {

    companion object {

        private const val ITEM_OPTION_REQUEST_CODE = 12452
    }

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

    private val listener = object : FileItemView.Listener {
        override fun onClick(entry: FileEntry) {
            when (entry) {
                is FileEntry.Folder -> {
                    fragmentManager?.run {
                        val newFragment = Builder().build { folder put entry.file.absolutePath }
                        FolderToFolderTransition.applyToFragment(newFragment)
                        FolderToFolderTransition.applyToFragment(this@FolderFragment)

                        beginTransaction().setReorderingAllowed(true)
                                .replace(id, newFragment)
                                .addToBackStack(null)
                                .commit()
                    }
                }
                is FileEntry.Playlist,
                is FileEntry.Audio -> folderController.playNext(entry)
            }
        }

        override fun onLongClick(entry: FileEntry) {
            ItemsMenuDialogFragment.newInstance(
                    listOf(
                            FolderItemOption.Header(context!!, entry),
                            FolderItemOption.PlayNext(entry),
                            FolderItemOption.AddToQueue(entry)
                    )
            )
                    .show(this@FolderFragment, ITEM_OPTION_REQUEST_CODE)
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

    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?
    ) {
        if (requestCode == ITEM_OPTION_REQUEST_CODE) {
            data?.setExtrasClassLoader(FolderFragment::class.java.classLoader)
            val item = data?.getParcelableExtra<FolderItemOption>(ItemsMenuDialogFragment.KEY_RESULT_ITEM)

            when (item) {
                is FolderFragment.FolderItemOption.Header -> Unit
                is FolderFragment.FolderItemOption.PlayNext -> folderController.playNext(item.entry)
                is FolderFragment.FolderItemOption.AddToQueue -> folderController.addToQueue(item.entry)
                null -> {}
            }.run { }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private sealed class FolderItemOption : ItemsMenuItem {

        @Parcelize
        data class Header(
                override val data: ItemsMenuFileHeaderView.Model.Data
        ) : FolderItemOption(), ItemsMenuFileHeaderView.Model {
            constructor(
                    context: Context,
                    entry: FileEntry
            ) : this(
                    ItemsMenuFileHeaderView.Model.Data(
                            entry.getDisplayTitle(context),
                            entry.getDescription(context),
                            entry.image
                    )
            )
        }

        @Parcelize
        data class PlayNext(
                val entry: FileEntry,
                override val data: ItemsMenuIconWithTextView.Model.Data = ItemsMenuIconWithTextView.Model.Data(
                        R.drawable.ic_queue_play_next_24dp,
                        R.string.play_next
                )
        ) : FolderItemOption(), ItemsMenuIconWithTextView.Model

        @Parcelize
        data class AddToQueue(
                val entry: FileEntry,
                override val data: ItemsMenuIconWithTextView.Model.Data = ItemsMenuIconWithTextView.Model.Data(
                        R.drawable.ic_playlist_add_24dp,
                        R.string.add_to_queue
                )
        ) : FolderItemOption(), ItemsMenuIconWithTextView.Model
    }
}