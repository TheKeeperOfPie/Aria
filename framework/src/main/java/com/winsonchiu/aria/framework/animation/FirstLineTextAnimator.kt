package com.winsonchiu.aria.framework.animation

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.ComposeShader
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.text.Editable
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.annotation.Px
import androidx.core.graphics.toRect
import androidx.core.graphics.withTranslation
import com.winsonchiu.aria.framework.util.animation.lerp
import com.winsonchiu.aria.framework.util.dpToPx
import com.winsonchiu.aria.framework.util.setAlpha
import com.winsonchiu.aria.framework.util.text.TextWatcherAdapter
import com.winsonchiu.aria.framework.view.DrawableDrawView

class FirstLineTextAnimator(
        @Px private val targetHeight: Int,
        private val sourceView: TextView,
        drawView: DrawableDrawView
) {
    private val drawable = TextDrawable()

    private var canvas: Canvas? = null
    private var bitmap: Bitmap? = null

    private var lastText: String? = null
    private var lastSavedObserver: ViewTreeObserver? = null

    private val onLayoutChangeListener = View
            .OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                if (v == sourceView
                        && (oldLeft != left
                                || oldTop != top
                                || oldRight != right
                                || oldBottom != bottom)) {
                    val width = right - left
                    val height = bottom - top
                    val oldBitmap = this.bitmap

                    this.bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    this.canvas = Canvas(bitmap!!)

                    oldBitmap?.recycle()

                    schedulePreDraw()
                }
            }

    private val textChangedListener = object : TextWatcherAdapter() {
        override fun afterTextChanged(s: Editable?) {
            val newText = s.toString()

            if (newText != lastText) {
                lastText = newText
                schedulePreDraw()
            }
        }
    }

    private val preDrawListener = object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            if (canvas != null) {
                updateValues()

                when {
                    lastSavedObserver?.isAlive == true -> lastSavedObserver?.removeOnPreDrawListener(this)
                    else -> sourceView.viewTreeObserver.removeOnPreDrawListener(this)
                }

                lastSavedObserver = null
            }

            return true
        }
    }

    init {
        sourceView.addOnLayoutChangeListener(onLayoutChangeListener)
        sourceView.addTextChangedListener(textChangedListener)
        drawView.addDrawable(drawable)
    }

    fun setProgress(
            progressHorizontal: Float,
            progressVertical: Float
    ) {
        drawable.setProgress(progressHorizontal = progressHorizontal, progressVertical = progressVertical)
    }

    private fun schedulePreDraw() {
        if (lastSavedObserver == null) {
            lastSavedObserver = sourceView.viewTreeObserver
            lastSavedObserver?.addOnPreDrawListener(preDrawListener)
        }
    }

    private fun updateValues() {
        bitmap?.eraseColor(Color.TRANSPARENT)
        sourceView.draw(canvas)
        drawable.updateValues()
        drawable.invalidateSelf()
    }

    private inner class TextDrawable : Drawable() {

        private val startBounds = Rect()
        private val endBounds = Rect()
        private val currentBounds = Rect()

        private val interpolator = DecelerateInterpolator()

        private val remainingBounds = Rect()
        private val remainingDrawBounds = Rect()

        private val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)
        private val remainingBoundsPaint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)

        private var linearGradient: LinearGradient? = null

        private val bitmapMatrix = Matrix()
        private val gradientMatrix = Matrix()

        private val gradientShift = 56f.dpToPx(sourceView)
        private val gradientLength = 24f.dpToPx(sourceView)

        private var canvasTranslateX = 0f
        private var canvasTranslateY = 0f

        private val bitmapTempBounds = Rect()
        private val bitmapBounds = RectF()
        private val bitmapBoundsStart = RectF()
        private val bitmapBoundsEnd = RectF()

        private var progressHorizontal = 0f
        private var progressVertical = 0f

        fun setProgress(
                progressHorizontal: Float,
                progressVertical: Float
        ) {
            this.progressHorizontal = progressHorizontal
            this.progressVertical = progressVertical
            updateProgress()
        }

        fun updateValues() {
            bitmap?.let {
                sourceView.getLineBounds(0, bitmapTempBounds)
                bitmapBoundsStart.set(bitmapTempBounds)
                bitmapBoundsStart.left = sourceView.layout.getLineLeft(0)
                bitmapBoundsStart.right = sourceView.layout.getLineRight(0)

                bitmapBoundsEnd.set(bitmapBoundsStart)
                bitmapBoundsEnd.bottom = bitmapBoundsEnd.top + targetHeight
                bitmapBoundsEnd
                        .right = bitmapBoundsStart.left + bitmapBoundsStart.width() * targetHeight / bitmapBoundsStart.height()

                val offsetLeft = sourceView.left
                val offsetTop = sourceView.top

                startBounds.set(bitmapBoundsStart.toRect())
                startBounds.offset(sourceView.left, sourceView.top)

                endBounds.set(
                        0, 0,
                        (bitmapBoundsStart.width() * targetHeight / bitmapBoundsStart.height()).toInt(), targetHeight
                )

                remainingBounds.set(
                        0,
                        bitmapBoundsStart.bottom.toInt(),
                        it.width,
                        it.height
                )

                remainingDrawBounds.set(
                        0,
                        (offsetTop + bitmapBoundsStart.bottom).toInt(),
                        offsetLeft + it.width,
                        offsetTop + it.height
                )

                linearGradient = LinearGradient(
                        it.width.toFloat(),
                        0f,
                        it.width + gradientLength,
                        0f,
                        Color.WHITE,
                        Color.TRANSPARENT,
                        Shader.TileMode.CLAMP
                )

                paint.shader = ComposeShader(
                        BitmapShader(it, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP),
                        linearGradient!!,
                        PorterDuff.Mode.SRC_IN
                )

                updateProgress()
            }
        }

        fun updateProgress() {
            val left = progressHorizontal.lerp(startBounds.left, endBounds.left)
            val top = progressHorizontal.lerp(startBounds.top, endBounds.top)
            val right = progressHorizontal.lerp(startBounds.right, endBounds.right)
            val bottom = progressHorizontal.lerp(startBounds.bottom, endBounds.bottom)

            val verticalTop = startBounds.top + ((endBounds.top - startBounds.top) * progressVertical).toInt()
            currentBounds.set(left, verticalTop, right, verticalTop + bottom - top)
            remainingDrawBounds.offsetTo(0, currentBounds.bottom)

            remainingBoundsPaint.setAlpha(1f - progressVertical)

            updateFirstLineValues()
            invalidateSelf()
        }

        fun updateFirstLineValues() {
            val left = progressHorizontal.lerp(bitmapBoundsStart.left, bitmapBoundsEnd.left)
            val top = progressHorizontal.lerp(bitmapBoundsStart.top, bitmapBoundsEnd.top)
            val right = progressHorizontal.lerp(bitmapBoundsStart.right, bitmapBoundsEnd.right)
            val bottom = progressHorizontal.lerp(bitmapBoundsStart.bottom, bitmapBoundsEnd.bottom)
            bitmapBounds.set(left, top, right, bottom)

            val scale = progressHorizontal.lerp(1f, bitmapBoundsEnd.height() / bitmapBoundsStart.height())
            bitmapMatrix.setTranslate(-bitmapBoundsStart.left, -bitmapBoundsStart.top)
            bitmapMatrix.postScale(scale, scale)
            bitmapMatrix.postTranslate(bitmapBoundsStart.left, bitmapBoundsStart.top)
            paint.shader?.setLocalMatrix(bitmapMatrix)

            val gradientTranslateX = progressHorizontal.lerp(0f, -gradientShift)
            gradientMatrix.setTranslate(gradientTranslateX, 0f)
            linearGradient?.setLocalMatrix(gradientMatrix)

            canvasTranslateX = progressHorizontal.lerp(0f, -bitmapBoundsStart.left - sourceView.left)
            canvasTranslateY = progressVertical.lerp(0f, -bitmapBoundsStart.top - sourceView.top)
        }

        override fun draw(canvas: Canvas) {
            bitmap?.let {
                canvas.drawBitmap(it, remainingBounds, remainingDrawBounds, remainingBoundsPaint)
                canvas.withTranslation(canvasTranslateX, canvasTranslateY) {
                    canvas.drawRect(bitmapBounds, paint)
                }
            }
        }

        override fun setAlpha(alpha: Int) {}

        override fun getOpacity() = PixelFormat.TRANSLUCENT

        override fun setColorFilter(colorFilter: ColorFilter?) {}
    }
}