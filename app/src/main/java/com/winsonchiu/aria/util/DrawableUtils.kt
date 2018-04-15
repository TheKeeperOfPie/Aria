package com.winsonchiu.aria.util

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
            getDrawableCompat(it, 0, context)!!
        }
    }

    fun getDrawableCompat(typedArray: TypedArray, index: Int, context: Context): Drawable? {
        val resourceId = typedArray.getResourceId(index, -1)
        return if (resourceId == -1) null else AppCompatResources.getDrawable(context, resourceId)
    }
}