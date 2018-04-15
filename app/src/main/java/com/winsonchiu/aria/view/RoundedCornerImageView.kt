package com.winsonchiu.aria.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.AttributeSet
import android.widget.ImageView

/**
 * Inspired by Romain Guy's blog here: http://www.curious-creature.com/2012/12/13/android-recipe-2-fun-with-shaders/
 *
 * This class assumes that the image passed in will be center cropped.
 * By overriding [ImageView.onDraw], some functionality may be lost, so do not
 * assume this class behaves exactly like an ImageView.
 *
 * Default corner radius of 2dp
 */
open class RoundedCornerImageView
@JvmOverloads
constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : RoundedForegroundImageView(context, attrs, defStyleAttr) {

    private var imagePaint = Paint().apply { isAntiAlias = true }
    private val imageBounds = RectF()
    private val imageBitmapMatrix = Matrix()
    private val imagePath = Path()
    private var imageShader: Shader? = null

    private var initialized = false

    private var bitmapWidth: Int = 0
    private var bitmapHeight: Int = 0

    init {
        initialized = true
        updateBounds()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)

        when (drawable) {
            is BitmapDrawable -> updateValues(drawable)
            is ColorDrawable -> updateValues(drawable)
            is GradientDrawable -> updateValues(drawable)
            else -> reset()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateBounds()
        invalidate()
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        updateBounds()
    }

    private fun updateBounds() {
        if (!initialized) {
            return
        }

        imageBounds.set(paddingLeft.toFloat(),
                paddingTop.toFloat(),
                (width - paddingRight).toFloat(),
                (height - paddingBottom).toFloat())

        if (drawable is BitmapDrawable) {
            updateBitmapMatrix()
        }

        updatePath()
    }

    private fun updateValues(drawable: ColorDrawable) {
        reset()
        imagePaint.color = drawable.color

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imagePaint.colorFilter = drawable.colorFilter
        }

        updatePath()
    }

    private fun updateValues(drawable: GradientDrawable) {
        reset()
        syncGradientDrawableRadii(drawable)
    }

    private fun updateValues(drawable: BitmapDrawable) {
        if (drawable.bitmap == null) {
            reset()
        } else {
            updateValues(drawable.bitmap, drawable.tileModeX, drawable.tileModeY)
            imagePaint.colorFilter = drawable.paint.colorFilter
        }
    }

    private fun updateValues(bitmap: Bitmap, tileModeX: Shader.TileMode?, tileModeY: Shader.TileMode?) {
        reset()

        bitmapWidth = bitmap.width
        bitmapHeight = bitmap.height

        imageShader = BitmapShader(bitmap, tileModeX ?: Shader.TileMode.CLAMP, tileModeY ?: Shader.TileMode.CLAMP)

        updateBitmapMatrix()
        updatePath()
    }

    private fun updateBitmapMatrix() {
        if (imageShader == null) {
            return
        }

        var translationX = imageBounds.left
        var translationY = imageBounds.top
        val scale: Float

        if (bitmapWidth * imageBounds.height() > imageBounds.width() * bitmapHeight) {
            scale = imageBounds.height() / bitmapHeight
            translationX += (imageBounds.width() - bitmapWidth * scale) * 0.5f
        } else {
            scale = imageBounds.width() / bitmapWidth
            translationY += (imageBounds.height() - bitmapHeight * scale) * 0.5f
        }

        imageBitmapMatrix.reset()
        imageBitmapMatrix.setScale(scale, scale)
        imageBitmapMatrix.postTranslate(translationX, translationY)

        imageShader!!.setLocalMatrix(imageBitmapMatrix)

        imagePaint.shader = imageShader
    }

    private fun updatePath() {
        val x1 = imageBounds.left
        val y1 = imageBounds.top
        val x2 = imageBounds.right
        val y2 = imageBounds.bottom

        imagePath.reset()
        imagePath.moveTo(0f, 0f)

        if (roundTopLeft) {
            imagePath.moveTo(x1 + radiusX, y1)
        } else {
            imagePath.moveTo(x1, y1)
        }

        if (roundTopRight) {
            imagePath.lineTo(x2 - radiusX, y1)
            imagePath.rQuadTo(radiusX, 0f, radiusX, radiusY)
        } else {
            imagePath.lineTo(x2, y1)
        }

        if (roundBottomRight) {
            imagePath.lineTo(x2, y2 - radiusY)
            imagePath.rQuadTo(0f, radiusY, -radiusX, radiusY)
        } else {
            imagePath.lineTo(x2, y2)
        }

        if (roundBottomLeft) {
            imagePath.lineTo(x1 + radiusX, y2)
            imagePath.rQuadTo(-radiusX, 0f, -radiusX, -radiusY)
        } else {
            imagePath.lineTo(x1, y2)
        }

        if (roundTopLeft) {
            imagePath.lineTo(x1, y1 + radiusY)
            imagePath.rQuadTo(0f, -radiusY, radiusX, -radiusY)
        } else {
            imagePath.lineTo(x1, y1)
        }

        imagePath.close()
    }

    private fun reset() {
        imageShader = null
        bitmapHeight = 0
        bitmapWidth = 0
        imagePaint.colorFilter = null
        imagePaint.shader = null
    }

    private fun syncGradientDrawableRadii(drawable: GradientDrawable) {
        val radii = FloatArray(8)

        if (roundTopLeft) {
            radii[0] = radiusX
            radii[1] = radiusY
        }

        if (roundTopRight) {
            radii[2] = radiusX
            radii[3] = radiusY
        }

        if (roundBottomRight) {
            radii[4] = radiusX
            radii[5] = radiusY
        }

        if (roundBottomLeft) {
            radii[6] = radiusX
            radii[7] = radiusY
        }

        drawable.cornerRadii = radii
    }

    override fun setImageBitmap(bitmap: Bitmap?) {
        super.setImageBitmap(bitmap)

        if (bitmap == null) {
            reset()
        } else {
            updateValues(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }
    }

    override fun setImageAlpha(alpha: Int) {
        imagePaint.alpha = alpha
        super.setImageAlpha(alpha)
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        super.setColorFilter(colorFilter)
        imagePaint.colorFilter = colorFilter
    }

    override fun onDraw(canvas: Canvas) {
        val drawable = drawable

        if (drawable is BitmapDrawable || drawable is ColorDrawable) {
            drawBackground(canvas)
            canvas.drawPath(imagePath, imagePaint)
            drawForeground(canvas)
        } else {
            super.onDraw(canvas)
        }
    }
}
