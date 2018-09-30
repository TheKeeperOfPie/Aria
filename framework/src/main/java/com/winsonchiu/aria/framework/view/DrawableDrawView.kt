package com.winsonchiu.aria.framework.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View

class DrawableDrawView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val callback = object : Drawable.Callback {
        override fun unscheduleDrawable(
                who: Drawable,
                what: Runnable
        ) {
            invalidate()
        }

        override fun scheduleDrawable(
                who: Drawable,
                what: Runnable,
                `when`: Long
        ) {
            invalidate()
        }

        override fun invalidateDrawable(who: Drawable) {
            invalidate()
        }
    }

    private var drawables = mutableListOf<Drawable>()

    init {
        setWillNotDraw(false)
    }

    fun addDrawable(drawable: Drawable) {
        drawable.callback = callback
        drawables.add(drawable)
    }

    fun clearDrawables() {
        drawables.clear()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawables.forEach { it.draw(canvas) }
    }
}