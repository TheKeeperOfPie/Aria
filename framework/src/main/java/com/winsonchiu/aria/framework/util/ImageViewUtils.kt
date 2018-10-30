package com.winsonchiu.aria.framework.util

import android.graphics.Matrix
import android.util.Log
import android.widget.ImageView
import com.winsonchiu.aria.framework.BuildConfig
import java.lang.reflect.Method

/**
 * Copy of internal [androidx.transition.ImageViewUtils]
 */
object ImageViewUtils {

    private val TAG = "ImageViewUtils"

    private var sAnimateTransformMethod: Method? = null
    private var sAnimateTransformMethodFetched: Boolean = false

    /**
     * Sets the matrix to animate the content of the image view.
     */
    fun animateTransform(
            view: ImageView,
            matrix: Matrix?
    ) {
        fetchAnimateTransformMethod()
        if (sAnimateTransformMethod != null) {
            try {
                sAnimateTransformMethod!!.invoke(view, matrix)
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    throw e
                }
            }

        }
    }

    fun centerCropMatrix(
            view: ImageView,
            imageWidth: Int? = view.drawable?.intrinsicWidth,
            imageHeight: Int? = view.drawable?.intrinsicHeight,
            viewWidth: Int = view.width,
            viewHeight: Int = view.height,
            scaleOverride: Float? = null
    ) = centerCropMatrix(
            imageWidth, imageHeight, viewWidth, viewHeight, scaleOverride
    )

    fun centerCropMatrix(
            imageWidth: Int?,
            imageHeight: Int?,
            viewWidth: Int,
            viewHeight: Int,
            scaleOverride: Float? = null
    ): Matrix? {
        imageWidth ?: return null
        imageHeight ?: return null

        val scaleX = viewWidth.toFloat() / imageWidth
        val scaleY = viewHeight.toFloat() / imageHeight

        val maxScale = scaleOverride ?: Math.max(scaleX, scaleY)

        val width = imageWidth * maxScale
        val height = imageHeight * maxScale
        val tx = (viewWidth - width) / 2f
        val ty = (viewHeight - height) / 2f

        val matrix = Matrix()
        matrix.postScale(maxScale, maxScale)
        matrix.postTranslate(tx, ty)
        return matrix
    }

    private fun fetchAnimateTransformMethod() {
        if (!sAnimateTransformMethodFetched) {
            try {
                sAnimateTransformMethod = ImageView::class.java.getDeclaredMethod(
                        "animateTransform",
                        Matrix::class.java
                )
                sAnimateTransformMethod!!.isAccessible = true
            } catch (e: NoSuchMethodException) {
                Log.i(TAG, "Failed to retrieve animateTransform method", e)
            }

            sAnimateTransformMethodFetched = true
        }
    }
}