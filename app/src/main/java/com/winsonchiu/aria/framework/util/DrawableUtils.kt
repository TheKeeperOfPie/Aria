package com.winsonchiu.aria.framework.util

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.support.v7.content.res.AppCompatResources
import androidx.core.content.res.use
import com.winsonchiu.aria.R

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