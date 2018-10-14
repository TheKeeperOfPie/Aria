package com.winsonchiu.aria.framework.view

import android.content.Context
import android.util.AttributeSet
import com.winsonchiu.aria.framework.util.RoundedOutlineProvider
import com.winsonchiu.aria.framework.util.dpToPx

class RoundedImageView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : MaskedImageView(context, attrs, defStyleAttr) {

    private var radius = 400f.dpToPx(this)

    init {
//        @Suppress("Recycle")
//        context.obtainStyledAttributes(intArrayOf(R.attr.radius)).use {
//            radius = it.getDimension(0, radius)
//        }

        elevation = 0f
        outlineProvider = RoundedOutlineProvider(400f)
        clipToOutline = true
    }
}