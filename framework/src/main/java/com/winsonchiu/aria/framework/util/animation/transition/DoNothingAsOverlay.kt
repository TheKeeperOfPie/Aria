package com.winsonchiu.aria.framework.util.animation.transition

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.View
import android.view.ViewGroup

/**
 * Used to keep a view onscreen during a Fragment exit transition.
 */
object DoNothingAsOverlay : GhostViewOverlay(
        overlayMode = OverlayMode.FRAMEWORK_VISIBILITY
) {

    override fun onCaptureStart(view: View, values: MutableMap<String, Any?>) {
        values["DoNothingAsOverlay"] = "start"
    }

    override fun onCaptureEnd(view: View, values: MutableMap<String, Any?>) {
        values["DoNothingAsOverlay"] = "end"
    }

    override fun onCreateAnimator(sceneRoot: ViewGroup, startView: View?, endView: View?, startValues: MutableMap<String, Any?>?, endValues: MutableMap<String, Any?>?): Animator? {
        return ValueAnimator.ofFloat(0f, 1f)
    }
}
