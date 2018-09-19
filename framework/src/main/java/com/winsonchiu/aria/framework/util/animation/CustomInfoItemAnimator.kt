package com.winsonchiu.aria.framework.util.animation

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

abstract class CustomInfoItemAnimator<ItemInfo : RecyclerView.ItemAnimator.ItemHolderInfo> : DefaultItemAnimator() {

    open fun captureStartValues(
            state: RecyclerView.State,
            viewHolder: RecyclerView.ViewHolder,
            changeFlags: Int,
            payloads: MutableList<Any>,
            itemInfo: ItemInfo
    ) {
    }

    open fun captureEndValues(
            state: RecyclerView.State,
            viewHolder: RecyclerView.ViewHolder,
            itemInfo: ItemInfo
    ) {
    }

    @Suppress("UNCHECKED_CAST")
    final override fun recordPreLayoutInformation(
            state: RecyclerView.State,
            viewHolder: RecyclerView.ViewHolder,
            changeFlags: Int,
            payloads: MutableList<Any>
    ) = super.recordPreLayoutInformation(state, viewHolder, changeFlags, payloads).apply {
        captureStartValues(state, viewHolder, changeFlags, payloads, this as ItemInfo)
    }

    @Suppress("UNCHECKED_CAST")
    final override fun recordPostLayoutInformation(
            state: RecyclerView.State,
            viewHolder: RecyclerView.ViewHolder
    ) = super.recordPostLayoutInformation(state, viewHolder).apply {
        captureEndValues(state, viewHolder, this as ItemInfo)
    }

    final override fun obtainHolderInfo() = buildInfoObject()

    abstract fun buildInfoObject(): ItemInfo
}
