package com.winsonchiu.aria.framework.util

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider

class RoundedOutlineProvider(
        var radius: Float
) : ViewOutlineProvider() {

    override fun getOutline(
            view: View,
            outline: Outline
    ) {
        outline.setRoundRect(0, 0, view.width, view.height, radius)
    }
}