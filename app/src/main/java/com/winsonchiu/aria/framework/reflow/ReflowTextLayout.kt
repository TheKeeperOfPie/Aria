package com.winsonchiu.aria.framework.reflow

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.transition.TransitionValues
import com.winsonchiu.aria.framework.view.ReflowText
import com.winsonchiu.aria.framework.view.ReflowTextView

class ReflowTextLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    lateinit var textStart: ReflowTextView
    lateinit var textEnd: TextView

    override fun onFinishInflate() {
        super.onFinishInflate()

        textStart = getChildAt(0) as ReflowTextView
        textEnd = getChildAt(1) as TextView
        textEnd.isInvisible = !isInEditMode
    }

    var text: CharSequence?
        get() = textStart.text
        set(value) {
            textStart.text = value
            textEnd.text = value

            textStart.disableDrawForReflow = false
            textStart.clearSwitchDrawables()

            textStart.viewTreeObserver.addOnPreDrawListener(titlePreDrawListener)
        }

    var progress
        get() = animator?.animatedFraction ?: 0f
        set(value) {
            animator?.setCurrentFraction(value)
        }

    private var animator: ValueAnimator? = null

    private val titlePreDrawListener = object : ViewTreeObserver.OnPreDrawListener {
        @SuppressLint("RestrictedApi")
        override fun onPreDraw(): Boolean {
            if (textStart.visibility != View.GONE && textStart.text.isNotEmpty()) {
                val startLaidOut = textStart.isLaidOut
                val endLaidOut = textEnd.isLaidOut
                val startWidthNonZero = textStart.measuredWidth > 0
                val startHeightNonZero = textStart.measuredHeight > 0
                val endWidthNonZero = textEnd.measuredWidth > 0
                val endHeightNonZero = textEnd.measuredHeight > 0

                if (startLaidOut
                        && endLaidOut
                        && startWidthNonZero
                        && startHeightNonZero
                        && endWidthNonZero
                        && endHeightNonZero) {
                    val reflowText = ReflowText()
                    val startValues = TransitionValues().apply {
                        view = textStart
                    }
                    val endValues = TransitionValues().apply {
                        view = textStart
                    }
                    reflowText.captureValues(startValues)
                    reflowText.captureValues(endValues)
                    animator = reflowText.createAnimator(this@ReflowTextLayout, startValues, endValues)!!

                    updateProgress()
                    textStart.viewTreeObserver.removeOnPreDrawListener(this)
                }
            } else {
                updateProgress()
                textStart.viewTreeObserver.removeOnPreDrawListener(this)
            }

            return true
        }
    }

    fun updateProgress() {
        animator?.setCurrentFraction(progress)
    }

}