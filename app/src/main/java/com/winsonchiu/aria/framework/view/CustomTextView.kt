package com.winsonchiu.aria.framework.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatTextView

class CustomTextView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    override fun onSizeChanged(
            width: Int,
            height: Int,
            oldWidth: Int,
            oldHeight: Int
    ) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        Log.d("CustomTextView", "onSizeChanged called with $width, $height, $oldWidth, $oldHeight")
    }

    override fun onMeasure(
            widthMeasureSpec: Int,
            heightMeasureSpec: Int
    ) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        Log.d("CustomTextView", "onMeasure called with $widthMeasureSpec, $heightMeasureSpec")
    }
}