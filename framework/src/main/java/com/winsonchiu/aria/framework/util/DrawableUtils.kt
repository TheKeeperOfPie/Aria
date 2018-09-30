package com.winsonchiu.aria.framework.util

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.use
import com.winsonchiu.aria.framework.R

object DrawableUtils {

    fun getDefaultRipple(context: Context, borderless: Boolean): Drawable {
        val attribute = if (borderless) R.attr.selectableItemBackgroundBorderless else R.attr.selectableItemBackground
        return context.obtainStyledAttributes(intArrayOf(attribute)).use {
            it.getDrawableCompat(0, context)!!
        }
    }
}

fun TypedArray.getDrawableCompat(index: Int, context: Context): Drawable? {
    val resourceId = getResourceId(index, -1)
    return if (resourceId == -1) null else AppCompatResources.getDrawable(context, resourceId)
}

fun Drawable.setAlpha(alpha: Float) {
    this.alpha = (alpha * 255).toInt().coerceIn(0, 255)
}

fun Paint.setAlpha(alpha: Float) {
    this.alpha = (alpha * 255).toInt().coerceIn(0, 255)
}