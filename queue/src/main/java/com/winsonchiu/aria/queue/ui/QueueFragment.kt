package com.winsonchiu.aria.queue.ui

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.SimpleEpoxyController
import com.winsonchiu.aria.framework.fragment.subclass.BaseFragment
import com.winsonchiu.aria.framework.util.setDataForView
import com.winsonchiu.aria.queue.MediaQueue
import com.winsonchiu.aria.queue.QueueEntry
import com.winsonchiu.aria.queue.QueueOp
import com.winsonchiu.aria.queue.R
import com.winsonchiu.aria.queue.ui.view.QueueItemView
import com.winsonchiu.aria.queue.ui.view.QueueItemViewModel_
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.queue_fragment.*
import javax.inject.Inject

class QueueFragment : BaseFragment<QueueFragmentDaggerComponent.ComponentProvider, QueueFragmentDaggerComponent>() {

    @Inject
    lateinit var mediaQueue: MediaQueue

    private var currentQueue: MediaQueue.Model = MediaQueue.Model()

    private val epoxyController = SimpleEpoxyController()

    private val itemTouchHelper: ItemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {

        override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
        ): Int {
            return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.START)
        }

        override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
        ): Boolean {
            val positionOne = viewHolder.adapterPosition
            val positionTwo = target.adapterPosition

            currentQueue = MediaQueue.Model(QueueOp.Move(positionOne, positionTwo).apply(currentQueue.queue, currentQueue.currentIndex))
            epoxyController.setModels(currentQueue.toViewModels())
            mediaQueue.push(QueueOp.Move(positionOne, positionTwo))
            return true
        }

        override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
            return 0.3f
        }

        override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
        ) {
            val removeIndex = viewHolder.adapterPosition
            currentQueue = MediaQueue.Model(QueueOp.Remove(removeIndex).apply(currentQueue.queue, currentQueue.currentIndex))
            epoxyController.setModels(currentQueue.toViewModels())
            mediaQueue.push(QueueOp.Remove(removeIndex))
        }
    })


    private val listener = object : QueueItemView.Listener {
        override fun onClick(queueEntry: QueueEntry) {
            mediaQueue.setCurrentItem(queueEntry)
        }

        override fun onStartDrag(view: QueueItemView) {
            itemTouchHelper.startDrag(queueRecycler.getChildViewHolder(view))
        }
    }

    override fun makeComponent(parentComponent: QueueFragmentDaggerComponent.ComponentProvider) =
            parentComponent.queueFragmentComponent()

    override fun injectSelf(component: QueueFragmentDaggerComponent) = component.inject(this)

    override val layoutId = R.layout.queue_fragment

    override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        itemTouchHelper.attachToRecyclerView(queueRecycler)

        imageUndo.setOnClickListener { mediaQueue.pop() }

        imagePlay.setOnClickListener {
            mediaQueue.playPauseActions.accept(Unit)
        }

        imageShuffle.setOnClickListener { mediaQueue.push(QueueOp.Shuffle()) }
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
                .id(it.content.toString(), it.timeAddedToQueue)
                .queueEntry(it)
                .showSelected(it == currentEntry)
                .title(it.metadata.title)
                .listener(listener)
    }
}