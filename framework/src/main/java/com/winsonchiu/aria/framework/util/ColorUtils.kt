package com.winsonchiu.aria.framework.util

import android.graphics.Color
import androidx.annotation.FloatRange
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.alpha
import androidx.palette.graphics.Palette

fun Palette.mostPopulous() = swatches.maxBy { it.population }

fun Int.withMaxAlpha() = ColorUtils.setAlphaComponent(this, 255)

fun Int.withAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float) = ColorUtils.setAlphaComponent(
        this,
        (alpha * 255).toInt().coerceIn(0, 255)
)


private val hsvArrayLocal: ThreadLocal<FloatArray> = object : ThreadLocal<FloatArray>() {
    override fun initialValue(): FloatArray {
        return FloatArray(3)
    }
}

fun Int.multiplyValue(factor: Float): Int {
    val array = hsvArrayLocal.get()!!
    Color.colorToHSV(this, array)
    array[2] = (array[2] * factor.coerceIn(0f, 1f))
    return Color.HSVToColor(this.alpha, array)
}

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