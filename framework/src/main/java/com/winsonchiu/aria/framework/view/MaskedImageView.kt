package com.winsonchiu.aria.framework.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.use
import androidx.core.graphics.withSave
import com.winsonchiu.aria.framework.R
import com.winsonchiu.aria.framework.util.dpToPx

class MaskedImageView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var path = Path()
    private var radius = 4f.dpToPx(this)

    init {
        context.obtainStyledAttributes(R.styleable.MaskedImageView).use {
            radius = it.getDimension(R.styleable.MaskedImageView_radius, radius)
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        if (width != oldWidth || height != oldHeight) {
            path.reset()
            path.addRoundRect(0f, 0f, width.toFloat(), height.toFloat(), radius, radius, Path.Direction.CW)
        }
    }

    override fun draw(canvas: Canvas) {
        canvas.withSave {
            canvas.clipPath(path)
            super.draw(canvas)
        }
    }
}