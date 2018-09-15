package com.winsonchiu.aria.queue

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.SimpleEpoxyController
import com.winsonchiu.aria.R
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
import javax.inject.Inject

class QueueFragment : BaseFragment<ActivityComponent, QueueFragmentDaggerComponent>() {

    @Inject
    lateinit var mediaQueue: MediaQueue

    private var currentQueue: MediaQueue.Model = MediaQueue.Model()

    private val epoxyController = SimpleEpoxyController()

    private val itemTouchHelper: ItemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.START)
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val queueItem = (viewHolder.itemView as QueueItemView).queueItem
            val positionOne = currentQueue.queue.indexOf(queueItem)
            val positionTwo = currentQueue.queue.indexOf((target.itemView as QueueItemView).queueItem)

            val newList = currentQueue.queue.toMutableList().apply {
                removeAt(positionOne)
                add(positionTwo, queueItem)
            }

            epoxyController.setModels(currentQueue.toViewModels())
            mediaQueue.set(newList)

            return true
        }

        override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
            return 0.3f
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = currentQueue.queue.indexOf((viewHolder.itemView as QueueItemView).queueItem)

            val newList = currentQueue.queue.toMutableList().apply {
                removeAt(position)
            }

            epoxyController.setModels(currentQueue.toViewModels())
            mediaQueue.set(newList)
        }
    })


    private val listener = object : QueueItemView.Listener {
        override fun onClick(queueItem: MediaQueue.QueueItem) {
            mediaQueue.setCurrentItem(queueItem)
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

        imagePlay.setOnClickListener {
            mediaQueue.playPauseActions.accept(Unit)
        }
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
                    currentQueue = it.first.copy()
                    epoxyController.setDataForView(queueRecycler, it.second)
                }
    }

    private fun MediaQueue.Model.toViewModels() = queue.map {
        QueueItemViewModel_()
                .id(it.file.path, it.timeAddedToQueue)
                .queueItem(it)
                .showSelected(it == currentItem)
                .title(FileUtils.getFileDisplayTitle(FileUtils.getFileSortKey(it.file)))
                .listener(listener)
    }
}