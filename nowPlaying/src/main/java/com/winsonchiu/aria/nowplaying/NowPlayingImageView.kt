package com.winsonchiu.aria.nowplaying

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.updatePadding

class NowPlayingImageView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    init {
        cropToPadding = true
    }

    fun setBottomPadding(bottomPadding: Float) {
        updatePadding(bottom = bottomPadding.toInt())
    }
}