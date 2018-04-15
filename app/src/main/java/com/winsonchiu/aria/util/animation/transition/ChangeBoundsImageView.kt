package com.winsonchiu.aria.util.animation.transition

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.Rect
import android.support.annotation.CallSuper
import android.support.transition.ChangeBounds
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.winsonchiu.aria.BuildConfig
import com.winsonchiu.aria.util.animation.AnimationUtils

/**
 * Transition between ImageViews with different padding. Mirrors a [ChangeBounds] animation for
 * translating between start and end positions.
 *
 * This will assume drawable bounds should be set to the space inside the view not taken up
 * by padding.
 */
class ChangeBoundsImageView(
        private val startPadding: Rect,
        private val endPadding: Rect
) : GeneralizedTransition() {

    constructor(
            startPadding: Int,
            endPadding: Int
    ) : this(
            startPadding.let { Rect(it, it, it, it) },
            endPadding.let { Rect(it, it, it, it) }
    )

    companion object {
        private val PROP_BOUNDS = "${BuildConfig.APPLICATION_ID}:${ChangeBoundsImageView::class.java.simpleName}:bounds"
    }

    override fun onCaptureStart(view: View, values: MutableMap<String, Any?>) {
        values[PROP_BOUNDS] = Rect(view.left, view.top, view.right, view.bottom)
    }

    override fun onCaptureEnd(view: View, values: MutableMap<String, Any?>) {
        values[PROP_BOUNDS] = Rect(view.left, view.top, view.right, view.bottom)
    }

    override fun onCreateAnimator(
            sceneRoot: ViewGroup,
            startView: View?,
            endView: View?,
            startValues: MutableMap<String, Any?>?,
            endValues: MutableMap<String, Any?>?
    ): Animator? {
        if (startValues == null || endValues == null) {
            return null
        }

        val startBounds = startValues[PROP_BOUNDS] as Rect
        val endBounds = endValues[PROP_BOUNDS] as Rect

        val startDrawableBounds = Rect(0, 0, startBounds.right - startBounds.left, startBounds.bottom - startBounds.top)
        val endDrawableBounds = Rect(0, 0, endBounds.right - endBounds.left, endBounds.bottom - endBounds.top)

        val view = (startView ?: endView) as? ImageView ?: return null
        val drawable = view.drawable ?: return null

        return ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener {
                val left = AnimationUtils.lerp(startBounds.left, endBounds.left, animatedFraction)
                val top = AnimationUtils.lerp(startBounds.top, endBounds.top, animatedFraction)
                val right = AnimationUtils.lerp(startBounds.right, endBounds.right, animatedFraction)
                val bottom = AnimationUtils.lerp(startBounds.bottom, endBounds.bottom, animatedFraction)

                view.setLeftTopRightBottom(left, top, right, bottom)

                val paddingLeft = AnimationUtils.lerp(startPadding.left, endPadding.left, animatedFraction)
                val paddingTop = AnimationUtils.lerp(startPadding.top, endPadding.top, animatedFraction)
                val paddingRight = AnimationUtils.lerp(startPadding.right, endPadding.right, animatedFraction)
                val paddingBottom = AnimationUtils.lerp(startPadding.bottom, endPadding.bottom, animatedFraction)

                view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)

                val boundsLeft = AnimationUtils.lerp(startDrawableBounds.left, endDrawableBounds.left, animatedFraction)
                val boundsTop = AnimationUtils.lerp(startDrawableBounds.top, endDrawableBounds.top, animatedFraction)
                val boundsRight =
                        AnimationUtils.lerp(startDrawableBounds.right, endDrawableBounds.right, animatedFraction)
                val boundsBottom =
                        AnimationUtils.lerp(startDrawableBounds.bottom, endDrawableBounds.bottom, animatedFraction)

                drawable.setBounds(boundsLeft, boundsTop, boundsRight, boundsBottom)

                // No idea why, but we can't render the drawable unless we reset it twice
                view.setImageDrawable(null)
                view.setImageDrawable(drawable)
            }
        }
    }

    @CallSuper
    override fun getListener(
            sceneRoot: ViewGroup,
            startView: View?,
            endView: View?,
            startValues: MutableMap<String, Any?>?,
            endValues: MutableMap<String, Any?>?,
            removeFunction: () -> Unit
    ): GeneralizedTransitionListener? {
        val parentListener = super.getListener(sceneRoot, startView, endView, startValues, endValues, removeFunction)
        val view = endView ?: startView
        return TransitionUtils.makeParentLayoutSupressionTransitionListener(view, parentListener, removeFunction)
                ?: parentListener
    }
}
