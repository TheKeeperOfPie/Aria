package com.winsonchiu.aria.artwork

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.annotation.Px
import com.squareup.picasso.Transformation

class ArtworkTransformation(
        @Px val targetSize: Int = 0
) : Transformation {

    companion object {
        private val RATIO_RANGE = 1.5..2.5
    }

    override fun key() = "${ArtworkTransformation::class.java.canonicalName}:$targetSize"

    private fun shouldResize(
            inWidth: Int,
            inHeight: Int,
            targetWidth: Int,
            targetHeight: Int
    ): Boolean {
        return (targetWidth != 0 && inWidth > targetWidth || targetHeight != 0 && inHeight > targetHeight)
    }

    override fun transform(source: Bitmap): Bitmap {
        val inWidth = source.width
        val inHeight = source.height

        if (targetSize == inWidth && targetSize == inHeight) {
            return source
        }

        var result = source

        var drawX = 0
        var drawY = 0
        var drawWidth = inWidth
        var drawHeight = inHeight

        val matrix = Matrix()

        val targetWidth = targetSize
        val targetHeight = targetSize

        // Keep aspect ratio if one dimension is set to 0
        val widthRatio = if (targetWidth != 0) targetWidth / inWidth.toFloat() else targetHeight / inHeight.toFloat()
        val heightRatio = if (targetHeight != 0) targetHeight / inHeight.toFloat() else targetWidth / inWidth.toFloat()
        val scaleX: Float
        val scaleY: Float
        when {
            widthRatio > heightRatio -> {
                val newSize = Math.ceil((inHeight * (heightRatio / widthRatio)).toDouble()).toInt()
                drawY = (inHeight - newSize) / 2
                drawHeight = newSize
                scaleX = widthRatio
                scaleY = targetHeight / drawHeight.toFloat()
            }
            widthRatio < heightRatio -> {
                val newSize = Math.ceil((inWidth * (widthRatio / heightRatio)).toDouble()).toInt()

                val ratio = inWidth.toFloat() / inHeight

                drawX = if (ratio in RATIO_RANGE) {
                    inWidth - newSize
                } else {
                    (inWidth - newSize) / 2
                }
                drawWidth = newSize
                scaleX = targetWidth / drawWidth.toFloat()
                scaleY = heightRatio
            }
            else -> {
                drawX = 0
                drawWidth = inWidth
                scaleY = heightRatio
                scaleX = scaleY
            }
        }
        if (shouldResize( inWidth, inHeight, targetWidth, targetHeight)) {
            matrix.preScale(scaleX, scaleY)
        }

        val newResult = Bitmap.createBitmap(result, drawX, drawY, drawWidth, drawHeight, matrix, true)
        if (newResult != result) {
            result.recycle()
            result = newResult
        }

        return result
    }
}