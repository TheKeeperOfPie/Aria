package com.winsonchiu.aria.framework.util.animation

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import androidx.annotation.Size
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.recyclerview.widget.RecyclerView

abstract class ValueAnimatorItemAnimator<ItemInfo : RecyclerView.ItemAnimator.ItemHolderInfo> :
    CustomInfoItemAnimator<ItemInfo>() {

    companion object {

        private val isRemovedMethod by lazy { RecyclerView.ViewHolder::class.java.getDeclaredMethod("isRemoved")!! }

        private fun RecyclerView.ViewHolder.removed() = isRemovedMethod.invoke(this) as Boolean
    }

    private val animations = ArrayList<Animator>()

    open fun onAdd(
            viewHolder: RecyclerView.ViewHolder,
            preLayoutInfo: ItemInfo?,
            postLayoutInfo: ItemInfo
    ): List<AnimatorDelegate<*>>? {
        return null
    }

    open fun onRemove(
            viewHolder: RecyclerView.ViewHolder,
            preLayoutInfo: ItemInfo,
            postLayoutInfo: ItemInfo?
    ): List<AnimatorDelegate<*>>? {
        return null
    }

    open fun onChange(
            holder: RecyclerView.ViewHolder,
            preInfo: ItemInfo,
            postInfo: ItemInfo
    ): List<AnimatorDelegate<*>>? {
        return null
    }

    @Suppress("UNCHECKED_CAST")
    final override fun animateAppearance(
            viewHolder: RecyclerView.ViewHolder,
            preLayoutInfo: ItemHolderInfo?,
            postLayoutInfo: ItemHolderInfo
    ): Boolean {
        return if (preLayoutInfo != null && (preLayoutInfo.left != postLayoutInfo.left || preLayoutInfo.top != postLayoutInfo.top)) {
            super.animateAppearance(viewHolder, preLayoutInfo, postLayoutInfo)
        } else {
            val delegates = onAdd(viewHolder, preLayoutInfo as ItemInfo?, postLayoutInfo as ItemInfo)
            if (delegates == null) {
                super.animateAppearance(viewHolder, preLayoutInfo, postLayoutInfo)
            } else {
                dispatchAddStarting(viewHolder)
                buildAnimator(delegates as List<AnimatorDelegate<in Number>>, postLayoutInfo) {
                    dispatchAddFinished(viewHolder)
                }
                false
            }
        }
    }

    final override fun animatePersistence(
            viewHolder: RecyclerView.ViewHolder,
            preInfo: ItemHolderInfo,
            postInfo: ItemHolderInfo
    ): Boolean {
        return super.animatePersistence(viewHolder, preInfo, postInfo)
    }

    @Suppress("UNCHECKED_CAST")
    final override fun animateDisappearance(
            viewHolder: RecyclerView.ViewHolder,
            preLayoutInfo: ItemHolderInfo,
            postLayoutInfo: ItemHolderInfo?
    ): Boolean {
        val oldLeft = preLayoutInfo.left
        val oldTop = preLayoutInfo.top
        val disappearingItemView = viewHolder.itemView
        val newLeft = postLayoutInfo?.left ?: disappearingItemView.left
        val newTop = postLayoutInfo?.top ?: disappearingItemView.top
        return if (!viewHolder.removed() && (oldLeft != newLeft || oldTop != newTop)) {
            super.animateDisappearance(viewHolder, preLayoutInfo, postLayoutInfo)
        } else {
            val delegates = onRemove(viewHolder, preLayoutInfo as ItemInfo, postLayoutInfo as ItemInfo?)
            if (delegates == null) {
                super.animateDisappearance(viewHolder, preLayoutInfo, postLayoutInfo)
            } else {
                dispatchRemoveStarting(viewHolder)
                buildAnimator(delegates as List<AnimatorDelegate<in Number>>, postLayoutInfo ?: preLayoutInfo) {
                    dispatchRemoveFinished(viewHolder)
                }
                false
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    final override fun animateChange(
            oldHolder: RecyclerView.ViewHolder,
            newHolder: RecyclerView.ViewHolder,
            preInfo: ItemHolderInfo,
            postInfo: ItemHolderInfo
    ): Boolean {
        if (oldHolder != newHolder) {
            return super.animateChange(oldHolder, newHolder, preInfo, postInfo)
        }

        val delegates = onChange(newHolder, preInfo as ItemInfo, postInfo as ItemInfo)
        return if (delegates == null) {
            super.animateChange(oldHolder, newHolder, preInfo, postInfo)
        } else {
            dispatchChangeStarting(newHolder, false)
            buildAnimator(delegates as List<AnimatorDelegate<in Number>>, postInfo) {
                dispatchChangeStarting(newHolder, false)
            }
            false
        }
    }

    override fun endAnimation(item: RecyclerView.ViewHolder) {
        super.endAnimation(item)
        item.itemView.animate()
                .cancel()
    }

    override fun endAnimations() {
        super.endAnimations()

        val iterator = animations.listIterator()
        while (iterator.hasNext()) {
            // Remove before cancelling to prevent concurrent modification
            val next = iterator.next()
            iterator.remove()
            next.cancel()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun buildAnimator(
            delegates: List<AnimatorDelegate<in Number>>,
            postInfo: ItemInfo,
            endAction: () -> Unit
    ) {
        val animators = delegates.map { delegate ->
            val first = delegate.values.first()
            val shouldSkip = delegate.values.fold(true) { equivalent, it ->
                equivalent && it == first
            }

            if (shouldSkip) {
                return@map null
            }

            val animator = when (first) {
                is Int -> ValueAnimator.ofInt(*(delegate.values as Array<Int>).toIntArray())
                is Float -> ValueAnimator.ofFloat(*(delegate.values as Array<Float>).toFloatArray())
                else -> throw IllegalArgumentException("Delegate value is invalid type")
            }

            animations.add(animator)

            animator.addUpdateListener { delegate.onUpdate(it.animatedValue as Number) }
            animator.doOnCancel {
                animations.remove(animator)
                delegate.onSetFinalValues(postInfo)
            }
            animator.doOnEnd {
                animations.remove(animator)
            }

            return@map animator
        }.filterNotNull()

        val set = AnimatorSet()
        set.playTogether(animators)
        set.doOnEnd { endAction() }
        set.start()
    }

    abstract inner class AnimatorDelegate<Value : Number>(
            @Size(min = 1)
            vararg val values: Value
    ) {
        abstract fun onUpdate(animatedValue: Value)

        abstract fun onSetFinalValues(itemInfo: ItemInfo)
    }

    inner class DelegateImpl<in Value : Number>(
            @Size(min = 1)
            vararg values: Value,
            private val onUpdate: (Value) -> Unit,
            private val onSetFinalValues: (ItemInfo) -> Unit
    ) : AnimatorDelegate<Value>(*values) {
        override fun onUpdate(animatedValue: Value) = onUpdate.invoke(animatedValue)

        override fun onSetFinalValues(itemInfo: ItemInfo) = onSetFinalValues.invoke(itemInfo)
    }
}
