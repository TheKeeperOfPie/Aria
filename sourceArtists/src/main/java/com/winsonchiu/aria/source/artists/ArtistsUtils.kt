package com.winsonchiu.aria.source.artists

import android.content.Context
import com.winsonchiu.aria.framework.util.dpToPx

internal object ArtistsUtils {

    fun spanCount(context: Context): Int {
        return (context.resources.displayMetrics.widthPixels / 120f.dpToPx(context)).toInt().coerceAtLeast(3)
    }
}