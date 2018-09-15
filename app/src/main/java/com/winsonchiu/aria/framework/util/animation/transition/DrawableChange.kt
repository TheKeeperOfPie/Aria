package com.winsonchiu.aria.framework.util.animation.transition

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.FloatRange
import androidx.core.animation.doOnEnd
import com.winsonchiu.aria.BuildConfig
import com.winsonchiu.aria.framework.util.animation.AnimationUtils
import java.lang.reflect.Method

class DrawableChange(
        private val drawableStart: Drawable? = null,
        private val drawableEnd: Drawable? = null,
        private val resetToOriginal: Boolean = true,
        private val overlapFade: Boolean = false,
        logTag: String? = null
) : GeneralizedTransition(logTag) {

    companion object {
        private var GET_TINT_METHOD: Method? = null

        private fun BitmapDrawable.getTint(): ColorStateList? {
            try {
                if (GET_TINT_METHOD == null) {
                    GET_TINT_METHOD = BitmapDrawable::class.java.getDeclaredMethod("getTint")
                }

                return GET_TINT_METHOD?.invoke(this) as ColorStateList?
            } catch (ignored: Exception) {
                if (BuildConfig.DEBUG) {
                    throw ignored
                }
            }

            return null
        }
    }

    override fun onCaptureStart(view: View, values: MutableMap<String, Any?>) {
        values["drawable"] = drawableStart
    }

    override fun onCaptureEnd(view: View, values: MutableMap<String, Any?>) {
        values["drawable"] = drawableEnd
    }

    override fun onCreateAnimator(
            sceneRoot: ViewGroup,
            startView: View?,
            endView: View?,
            startValues: MutableMap<String, Any?>?,
            endValues: MutableMap<String, Any?>?
    ): Animator? {
        val view = (endView ?: startView) as? ImageView ?: return null
        val drawableStart = drawableStart ?: view.drawable
        val drawableEnd = drawableEnd ?: view.drawable
        val drawable = CrossFadeDrawable(drawableStart, drawableEnd, overlapFade)

        val drawableOriginal = view.drawable
        val colorStateList = (drawableOriginal as? BitmapDrawable)?.getTint()

        view.visibility = View.VISIBLE
        view.setImageDrawable(drawable)

        return ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener {
                drawable.setProgress(animatedFraction)
            }

            if (resetToOriginal) {
                doOnEnd {
                    view.setImageDrawable(drawableOriginal)
                    drawableOriginal.alpha = 255
                    colorStateList?.let(drawableOriginal::setTintList)
                    view.setImageDrawable(drawableOriginal)
                }
            }
        }
    }
}

class CrossFadeDrawable(
        private val drawableStart: Drawable?,
        private val drawableEnd: Drawable?,
        private val overlapFade: Boolean
) : Drawable() {

    init {
        drawableStart?.alpha = 255
        drawableEnd?.alpha = 0
    }

    fun setProgress(@FloatRange(from = 0.0, to = 1.0) progress: Float) {
        if (overlapFade) {
            drawableStart?.alpha = ((1f - progress) * 255).toInt().coerceIn(0, 255)
            drawableEnd?.alpha = (progress * 255).toInt().coerceIn(0, 255)
        } else {
            drawableStart?.alpha = (AnimationUtils.shiftRange(0.5f, 1f, 1f - progress) * 255).toInt().coerceIn(0, 255)
            drawableEnd?.alpha = (AnimationUtils.shiftRange(0.5f, 1f, progress) * 255).toInt().coerceIn(0, 255)
        }

        invalidateSelf()
    }

    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds)
        if (bounds != null
                && bounds.width() > 1
                && bounds.height() > 1
                && (bounds != drawableStart?.bounds || bounds != drawableEnd?.bounds)
        ) {
            drawableStart?.bounds = bounds
            drawableEnd?.bounds = bounds
        }
        invalidateSelf()
    }

    override fun draw(canvas: Canvas) {
        drawableStart?.draw(canvas)
        drawableEnd?.draw(canvas)
    }

    override fun setAlpha(alpha: Int) {}

    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter?) {}
}
