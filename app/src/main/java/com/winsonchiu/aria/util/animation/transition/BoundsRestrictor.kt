package com.winsonchiu.aria.util.animation.transition

import android.animation.Animator
import android.graphics.Rect
import android.support.annotation.CallSuper
import android.view.View
import android.view.ViewGroup
import com.winsonchiu.aria.BuildConfig
import com.winsonchiu.aria.util.animation.transition.BoundsRestrictor.RestrictMode.END
import com.winsonchiu.aria.util.animation.transition.BoundsRestrictor.RestrictMode.NONE
import com.winsonchiu.aria.util.animation.transition.BoundsRestrictor.RestrictMode.START

/**
 * Restricts the bounds of transitioning view to either the start or end measurements set by
 * TransitionManager.
 *
 * Main use case is when you need to shared element translate multiple views so that Z order is
 * maintained, but not all of them are actually moving during the transition. This can be used to
 * lock those non-translating views into their initial/final bounds.
 */
open class BoundsRestrictor(
        private val restrictMode: RestrictMode,
        overlayMode: OverlayMode = OverlayMode.NONE
) : GhostViewOverlay(overlayMode) {

    enum class RestrictMode {
        NONE, START, END
    }

    companion object {
        private val PROP_BOUNDS = "${BuildConfig.APPLICATION_ID}:${BoundsRestrictor::class.java.simpleName}:bounds"
    }

    @CallSuper
    override fun onCaptureStart(view: View, values: MutableMap<String, Any?>) {
        values[PROP_BOUNDS] = Rect(view.left, view.top, view.right, view.bottom)
    }

    @CallSuper
    override fun onCaptureEnd(view: View, values: MutableMap<String, Any?>) {
        values[PROP_BOUNDS] = Rect(view.left, view.top, view.right, view.bottom)
    }

    override fun onBeforeCreateAnimator(sceneRoot: ViewGroup, startView: View?, endView: View?, startValues: MutableMap<String, Any?>?, endValues: MutableMap<String, Any?>?) {
        when (restrictMode) {
            NONE -> Unit
            START -> startValues?.let { (startView ?: endView)?.setLeftTopRightBottom(it[PROP_BOUNDS] as Rect) }
            END -> endValues?.let { (endView ?: startView)?.setLeftTopRightBottom(it[PROP_BOUNDS] as Rect) }
        }
    }

    override fun onCreateAnimator(sceneRoot: ViewGroup, startView: View?, endView: View?, startValues: MutableMap<String, Any?>?, endValues: MutableMap<String, Any?>?): Animator? {
        // The relevant logic is done inside onBeforeCreateAnimator, so we don't need to run an animation.
        return null
    }

    @CallSuper
    override fun getListener(sceneRoot: ViewGroup, startView: View?, endView: View?, startValues: MutableMap<String, Any?>?, endValues: MutableMap<String, Any?>?, removeFunction: () -> Unit): GeneralizedTransitionListener? {
        val parentListener = super.getListener(sceneRoot, startView, endView, startValues, endValues, removeFunction)

        val view = when (restrictMode) {
            NONE -> return parentListener
            START -> startView ?: endView
            END -> endView ?: startView
        }

        return TransitionUtils.makeParentLayoutSupressionTransitionListener(view, parentListener, removeFunction) ?: parentListener
    }
}
