package com.winsonchiu.aria.framework.reflow

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isInvisible
import androidx.core.widget.TextViewCompat
import com.winsonchiu.aria.framework.view.ReflowTextView

class ReflowTextLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    lateinit var textStart: ReflowTextView
    lateinit var textEnd: ReflowTextView

    override fun onFinishInflate() {
        super.onFinishInflate()

        textStart = getChildAt(0) as ReflowTextView
        textEnd = getChildAt(1) as ReflowTextView
        textStart.isInvisible = !isInEditMode
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

    @SuppressLint("RestrictedApi")
    @TargetApi(Build.VERSION_CODES.O)
    fun AppCompatTextView.forceAutoSizeCalculation() {
        if (TextViewCompat.getAutoSizeTextType(this) != TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM) {
            return
        }

        val autoSizeMinTextSize = TextViewCompat.getAutoSizeMinTextSize(this)
        val autoSizeMaxTextSize = TextViewCompat.getAutoSizeMaxTextSize(this)
        val autoSizeStepGranularity = TextViewCompat.getAutoSizeStepGranularity(this).coerceAtLeast(1)
        setAutoSizeTextTypeUniformWithConfiguration(autoSizeMinTextSize, autoSizeMaxTextSize, autoSizeStepGranularity, TypedValue.COMPLEX_UNIT_PX)
    }

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
//                    val reflowText = ReflowText()
//                    val startValues = TransitionValues().apply {
//                        view = textStart
//                    }
//                    val endValues = TransitionValues().apply {
//                        view = textEnd
//                    }
//                    reflowText.captureValues(startValues)
//                    reflowText.captureValues(endValues)
//                    animator = reflowText.createAnimator(this@ReflowTextLayout, startValues, endValues)!!

                    textStart.forceAutoSizeCalculation()
                    textEnd.forceAutoSizeCalculation()

//                    val reflowAnimator = ReflowAnimator(textEnd, textStart)
//                    animator = reflowAnimator.createAnimator()

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