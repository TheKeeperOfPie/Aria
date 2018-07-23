package com.winsonchiu.aria.framework.util.animation.transition

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.Color
import android.support.transition.Fade
import android.support.transition.Slide
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.winsonchiu.aria.framework.util.animation.AnimationUtils
import com.winsonchiu.aria.framework.util.findChild

/**
 * The do everything transition. Handles customizable equivalents for [Slide] and [Fade] plus
 * scale along a pivot.
 *
 * This includes everything because combining multiple transitions on a single view can have
 * unintended behavior, such as multiple copies of that view inside an overlay, one for
 * each transition added.
 *
 * This is also a little easier to read as you can define the transition values for a view
 * in one place, rather than across multiple transitions.
 */
open class SlideAndFade(
        private val slideFractionFromX: Float = 0f,
        private val slideFractionToX: Float = 0f,
        private val slideFractionFromY: Float = 0f,
        private val slideFractionToY: Float = 0f,
        private val slideProgressFrom: Float = 0f,
        private val slideProgressTo: Float = 1f,

        private val fadeFrom: Float = 1f,
        private val fadeTo: Float = 1f,
        private val fadeProgressFrom: Float = 0f,
        private val fadeProgressTo: Float = 1f,

        private val scaleProgressFrom: Float = 0f,
        private val scaleProgressTo: Float = 1f,

        scaleFrom: Float = 1f,
        private val scaleFromX: Float = scaleFrom,
        private val scaleFromY: Float = scaleFrom,

        scaleTo: Float = 1f,
        private val scaleToX: Float = scaleTo,
        private val scaleToY: Float = scaleTo,

        private val pivotFractionX: Float = 0.5f,
        private val pivotFractionY: Float = 0.5f,

        private val slideRawFromX: Float? = null,
        private val slideRawToX: Float? = null,
        private val slideRawFromY: Float? = null,
        private val slideRawToY: Float? = null,

        private val onlyEndView: Boolean = false,

        private val target: String? = null,

        private val test: Boolean = false,

        forceVisible: Boolean = true,

        private val forceStartVisible: Boolean = forceVisible,
        private val forceEndVisible: Boolean = forceVisible,
        restrictMode: RestrictMode = RestrictMode.NONE,
        overlayMode: OverlayMode = OverlayMode.NONE
) : BoundsRestrictor(restrictMode, overlayMode) {

    override fun onCaptureStart(view: View, values: MutableMap<String, Any?>) {
        super.onCaptureStart(view, values)
        values["slideFractionX"] = slideFractionFromX
        values["slideFractionY"] = slideFractionFromY
        values["slideProgress"] = slideProgressFrom

        values["alpha"] = fadeFrom
        values["alphaProgress"] = fadeProgressFrom

        values["scaleX"] = scaleFromX
        values["scaleY"] = scaleFromY
        values["scaleProgress"] = scaleProgressFrom
    }

    override fun onCaptureEnd(view: View, values: MutableMap<String, Any?>) {
        super.onCaptureEnd(view, values)
        values["slideFractionX"] = slideFractionToX
        values["slideFractionY"] = slideFractionToY
        values["slideProgress"] = slideProgressTo

        values["alpha"] = fadeTo
        values["alphaProgress"] = fadeProgressTo

        values["scaleX"] = scaleToX
        values["scaleY"] = scaleToY
        values["scaleProgress"] = scaleProgressTo
    }

    override fun onCreateAnimator(sceneRoot: ViewGroup, startView: View?, endView: View?, startValues: MutableMap<String, Any?>?, endValues: MutableMap<String, Any?>?): Animator? {
        if (forceStartVisible) {
            startView?.visibility = View.VISIBLE
        }

        if (forceEndVisible) {
            endView?.visibility = View.VISIBLE
        }

        if (onlyEndView) {
            if (endView == null) {
                return null
            }
        }

        val view = if (target != null) {
            Log.d("SlideAndFade", "onCreateAnimator called with target = $target, endView = $endView, startView = $startView")
            val viewGroup = (endView ?: startView) as? ViewGroup ?: return null
            viewGroup.findChild {
                it.transitionName == target
            }
        } else {
            endView ?: startView ?: return null
        } ?: return null

        if (test) {
            view.setBackgroundColor(Color.RED)
        }

        val startTranslationX = slideRawFromX ?: slideFractionFromX * sceneRoot.width
        val endTranslationX = slideRawToX ?: slideFractionToX * sceneRoot.width

        val startTranslationY = slideRawFromY ?: slideFractionFromY * sceneRoot.height
        val endTranslationY = slideRawToY ?: slideFractionToY * sceneRoot.height

        view.apply {
            pivotX = pivotFractionX * view.width
            pivotY = pivotFractionY * view.height

            scaleX = scaleFromX
            scaleY = scaleFromY

            translationX = startTranslationX
            translationY = startTranslationY

            setTransitionAlpha(fadeFrom)
        }

        return ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener {
                val slideProgress = AnimationUtils.shiftRange(slideProgressFrom, slideProgressTo, animatedFraction)
                val alphaProgress = AnimationUtils.shiftRange(fadeProgressFrom, fadeProgressTo, animatedFraction)
                val scaleProgress = AnimationUtils.shiftRange(scaleProgressFrom, scaleProgressTo, animatedFraction)

                val translationX = AnimationUtils.lerp(startTranslationX, endTranslationX, slideProgress)
                val translationY = AnimationUtils.lerp(startTranslationY, endTranslationY, slideProgress)

                val alpha = AnimationUtils.lerp(fadeFrom, fadeTo, alphaProgress)

                val scaleX = AnimationUtils.lerp(scaleFromX, scaleToX, scaleProgress)
                val scaleY = AnimationUtils.lerp(scaleFromY, scaleToY, scaleProgress)

                view.scaleX = scaleX
                view.scaleY = scaleY
                view.translationX = translationX
                view.translationY = translationY
                view.setTransitionAlpha(alpha)
            }
        }
    }
}
