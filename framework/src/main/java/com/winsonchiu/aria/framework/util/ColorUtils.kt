package com.winsonchiu.aria.framework.util

import androidx.annotation.FloatRange
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette

fun Palette.mostPopulous() = swatches.maxBy { it.population }

fun Int.withMaxAlpha() = ColorUtils.setAlphaComponent(this, 255)

fun Int.withAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float) = ColorUtils.setAlphaComponent(
        this,
        (alpha * 255).toInt().coerceIn(0, 255)
)

object ColorUtils {

    fun crossFadeOver(
            start: Int,
            end: Int,
            progress: Float
    ) = ColorUtils.compositeColors(
            end.withAlpha(progress),
            start.withAlpha(1f - progress)
    )
}