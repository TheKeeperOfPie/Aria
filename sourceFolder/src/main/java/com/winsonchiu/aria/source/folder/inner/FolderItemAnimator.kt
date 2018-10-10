package com.winsonchiu.aria.source.folder.inner

import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyViewHolder
import com.winsonchiu.aria.framework.util.animation.ValueAnimatorItemAnimator
import com.winsonchiu.aria.source.folder.inner.view.FileItemView
import com.winsonchiu.aria.source.folder.inner.view.FileItemViewModel_
import kotlinx.android.synthetic.main.file_item_view.view.*

class FolderItemAnimator : ValueAnimatorItemAnimator<FolderItemAnimator.ItemInfo>() {

    override fun buildInfoObject() = ItemInfo()

    override fun onChange(
            holder: RecyclerView.ViewHolder,
            preInfo: ItemInfo,
            postInfo: ItemInfo
    ): List<AnimatorDelegate<*>>? {
        if ((holder as? EpoxyViewHolder)?.model is FileItemViewModel_) {
            val fileItemView = holder.itemView as FileItemView
            return listOf(
                    DelegateImpl(preInfo.imageAlpha, postInfo.imageAlpha,
                            onUpdate = { fileItemView.fileImage.imageAlpha = it },
                            onSetFinalValues = { fileItemView.fileImage.imageAlpha = it.imageAlpha })
            )
        }

        return null
    }

    override fun captureStartValues(
            state: RecyclerView.State,
            viewHolder: RecyclerView.ViewHolder,
            changeFlags: Int,
            payloads: MutableList<Any>,
            itemInfo: ItemInfo
    ) {
        val model = (viewHolder as? EpoxyViewHolder)?.model as? FileItemViewModel_ ?: return
        itemInfo.imageAlpha = if (model.entry().image == null) 0 else 255
    }

    override fun captureEndValues(
            state: RecyclerView.State,
            viewHolder: RecyclerView.ViewHolder,
            itemInfo: ItemInfo
    ) {
        val model = (viewHolder as? EpoxyViewHolder)?.model as? FileItemViewModel_ ?: return
        itemInfo.imageAlpha = if (model.entry().image == null) 0 else 255
    }

    data class ItemInfo(
            var imageAlpha: Int = 0
    ) : ItemHolderInfo()
}
