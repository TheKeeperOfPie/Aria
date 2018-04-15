package com.winsonchiu.aria.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import com.squareup.picasso.Callback
import com.winsonchiu.aria.R
import com.winsonchiu.aria.util.getDrawableCompat

@Suppress("LeakingThis")
open class ForegroundImageView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var initialBackground: Drawable? = null

    private var foregroundCompat: Drawable? = null

    private var callback: Callback? = null

    init {
        if (attrs != null) {
            val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.ForegroundImageView, defStyleAttr, 0)
            initialBackground = typedArray.getDrawableCompat(R.styleable.ForegroundImageView_android_background, context)
            setForegroundCompat(typedArray.getDrawableCompat(R.styleable.ForegroundImageView_android_foreground, context))
            typedArray.recycle()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (foregroundCompat != null) {
            foregroundCompat!!.setBounds(0, 0, w, h)
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawForeground(canvas)
    }

    fun setForegroundResource(drawableResId: Int) {
        setForegroundCompat(AppCompatResources.getDrawable(context, drawableResId))
    }

    override fun setForeground(foreground: Drawable?) {
        setForegroundCompat(foreground)
    }

    open fun setForegroundCompat(drawable: Drawable?) {
        if (foregroundCompat == drawable) {
            return
        }

        if (foregroundCompat != null) {
            foregroundCompat!!.callback = null
            unscheduleDrawable(foregroundCompat)
        }

        foregroundCompat = drawable

        if (drawable != null) {
            drawable.callback = this
            if (drawable.isStateful) {
                drawable.state = drawableState
            }
        }
        requestLayout()
        invalidate()
    }

    override fun getForeground(): Drawable? {
        return foregroundCompat
    }

    fun getForegroundCompat(): Drawable? {
        return foregroundCompat
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return super.verifyDrawable(who) || who === foregroundCompat
    }

    override fun jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState()

        if (foregroundCompat != null) {
            foregroundCompat!!.jumpToCurrentState()
        }
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()

        if (foregroundCompat != null && foregroundCompat!!.isStateful) {
            foregroundCompat!!.state = drawableState
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (foregroundCompat != null) {
            foregroundCompat!!.setBounds(0, 0, measuredWidth, measuredHeight)
            invalidate()
        }
    }

    fun getInitialBackground(): Drawable? {
        return initialBackground
    }

    protected fun drawForeground(canvas: Canvas) {
        if (foregroundCompat != null && foregroundCompat!!.isVisible) {
            foregroundCompat!!.draw(canvas)
        }
    }

    fun setForegroundAlpha(alpha: Int) {
        if (foregroundCompat != null) {
            foregroundCompat!!.alpha = alpha
            invalidate()
        }
    }
}
