package com.winsonchiu.aria.framework.util.animation

import androidx.annotation.FloatRange

object AnimationUtils {

    /**
     * Interpolate linearly between start and end values at a given point between 0 and 1f
     */
    fun lerp(start: Int, end: Int, @FloatRange(from = 0.0, to = 1.0) progress: Float): Int {
        return (start + (end - start) * progress).toInt()
    }

    /**
     * @see AnimationUtils.lerp
     */
    fun lerp(start: Float, end: Float, @FloatRange(from = 0.0, to = 1.0) progress: Float): Float {
        return start + (end - start) * progress
    }

    fun shiftRange(start: Float, end: Float, progress: Float): Float {
        return if (progress >= end) {
            1f
        } else if (progress <= start) {
            0f
        } else {
            (progress - start) / (end - start)
        }
    }
}