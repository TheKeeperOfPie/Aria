package com.winsonchiu.aria.queue

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import com.airbnb.epoxy.EpoxyModelTouchCallback
import com.airbnb.epoxy.EpoxyTouchHelper
import com.airbnb.epoxy.SimpleEpoxyController
import com.winsonchiu.aria.R
import com.winsonchiu.aria.R.id.queueRecycler
import com.winsonchiu.aria.folders.util.FileUtils
import com.winsonchiu.aria.framework.dagger.activity.ActivityComponent
import com.winsonchiu.aria.framework.fragment.subclass.BaseFragment
import com.winsonchiu.aria.framework.util.setDataForView
import com.winsonchiu.aria.media.MediaQueue
import com.winsonchiu.aria.queue.view.QueueItemView
import com.winsonchiu.aria.queue.view.QueueItemViewModel_
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.queue_fragment.*
import java.util.*
import javax.inject.Inject

class QueueFragment : BaseFragment<ActivityComponent, QueueFragmentDaggerComponent>() {

    @Inject
    lateinit var mediaQueue: MediaQueue

    private var currentQueue: MutableList<MediaQueue.QueueItem> = mutableListOf()

    private val epoxyController = SimpleEpoxyController()

    private val itemTouchHelper: ItemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {

        override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
            return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.END)
        }

        override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val queueItem = (viewHolder.itemView as QueueItemView).queueItem
            val positionOne = currentQueue.indexOf(queueItem)
            val positionTwo = currentQueue.indexOf((target.itemView as QueueItemView).queueItem)

            currentQueue = currentQueue.toMutableList().apply {
                removeAt(positionOne)
                add(positionTwo, queueItem)
            }

            epoxyController.setModels(currentQueue.toViewModels())
            mediaQueue.set(currentQueue)

            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = currentQueue.indexOf((viewHolder.itemView as QueueItemView).queueItem)

            currentQueue = currentQueue.toMutableList()
                    .apply {
                        removeAt(position)
                    }

            epoxyController.setModels(currentQueue.toViewModels())
            mediaQueue.set(currentQueue)
        }
    })


    private val listener = object : QueueItemView.Listener {
        override fun onClick(queueItem: MediaQueue.QueueItem) {
            // TODO
        }

        override fun onStartDrag(view: QueueItemView) {
            itemTouchHelper.startDrag(queueRecycler.getChildViewHolder(view))
        }
    }

    override fun makeComponent(parentComponent: ActivityComponent) =
            parentComponent.queueFragmentComponent()

    override fun injectSelf(component: QueueFragmentDaggerComponent) = component.inject(this)

    override val layoutId = R.layout.queue_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        itemTouchHelper.attachToRecyclerView(queueRecycler)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        itemTouchHelper.attachToRecyclerView(null)
    }

    override fun onStart() {
        super.onStart()

        mediaQueue.queueUpdates
                .observeOn(Schedulers.computation())
                .distinctUntilChanged { _, newQueue ->
                    newQueue == currentQueue
                }
                .map {
                    it to it.toViewModels()
                }
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle()
                .subscribe {
                    currentQueue = it.first.toMutableList()
                    epoxyController.setDataForView(queueRecycler, it.second)
                }
    }

    private fun List<MediaQueue.QueueItem>.toViewModels() = map {
        QueueItemViewModel_()
                .id(it.file.path)
                .queueItem(it)
                .title(FileUtils.getFileDisplayTitle(FileUtils.getFileSortKey(it.file)))
                .listener(listener)
    }
}