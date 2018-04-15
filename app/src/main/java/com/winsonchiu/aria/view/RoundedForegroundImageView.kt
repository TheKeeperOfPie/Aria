package com.winsonchiu.aria.view

import android.content.Context
import android.graphics.BitmapShader
import android.graphics.Canvas
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
import android.support.annotation.DrawableRes
import android.support.v7.content.res.AppCompatResources
import android.util.AttributeSet
import com.winsonchiu.aria.R
import com.winsonchiu.aria.util.dpToPx
import com.winsonchiu.aria.util.getDrawableCompat

open class RoundedForegroundImageView
@JvmOverloads
constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ForegroundImageView(context, attrs, defStyleAttr) {

    companion object {
        val FLAG_TOP_LEFT = 0x1
        val FLAG_TOP_RIGHT = 0x2
        val FLAG_BOTTOM_LEFT = 0x4
        val FLAG_BOTTOM_RIGHT = 0x8
    }

    var radiusX: Float = 0.toFloat()
    var radiusY: Float = 0.toFloat()

    protected var roundTopLeft = true
    protected var roundTopRight = true
    protected var roundBottomLeft = true
    protected var roundBottomRight = true

    private val backgroundPaint = Paint().apply { isAntiAlias = true }
    private val backgroundBounds = RectF()
    private val backgroundBitmapMatrix = Matrix()
    private val backgroundPath = Path()
    private var backgroundShader: Shader? = null
    private var backgroundDrawable: Drawable? = null

    private var initialized = false

    private var bitmapWidth: Int = 0
    private var bitmapHeight: Int = 0

    init {
        var radius = 2f.dpToPx(context)
        var background: Drawable? = null
        var foreground: Drawable? = null

        if (attrs != null) {
            val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.RoundedForegroundImageView, defStyleAttr, 0)
            radius = typedArray.getDimension(R.styleable.RoundedForegroundImageView_cornerRadius, radius)
            radiusX = typedArray.getDimension(R.styleable.RoundedForegroundImageView_cornerRadiusX, -1f)
            radiusY = typedArray.getDimension(R.styleable.RoundedForegroundImageView_cornerRadiusY, -1f)
            background = typedArray.getDrawableCompat(R.styleable.RoundedForegroundImageView_android_background, context)
            foreground = typedArray.getDrawableCompat(R.styleable.RoundedForegroundImageView_android_foreground, context)
            val corners = typedArray.getInt(R.styleable.RoundedForegroundImageView_cornersRounded, -1)
            typedArray.recycle()

            if (corners != -1) {
                roundTopLeft = corners and FLAG_TOP_LEFT == FLAG_TOP_LEFT
                roundTopRight = corners and FLAG_TOP_RIGHT == FLAG_TOP_RIGHT
                roundBottomLeft = corners and FLAG_BOTTOM_LEFT == FLAG_BOTTOM_LEFT
                roundBottomRight = corners and FLAG_BOTTOM_RIGHT == FLAG_BOTTOM_RIGHT
            }
        }

        if (radiusX <= 0 || radiusY <= 0) {
            radiusX = radius
            radiusY = radius
        }

        initialized = true

        updateBounds()
        foreground?.let(this::setForegroundCompat)
        setBackground(background)
    }

    fun setRadius(radius: Float) = setRadius(radius, radius)

    fun setRadius(radiusX: Float, radiusY: Float) {
        this.radiusX = radiusX
        this.radiusY = radiusY
        drawable?.let(::setImageDrawable)
    }

    override fun setForegroundCompat(drawable: Drawable?) {
        if (!initialized) {
            return
        }

        val roundedDrawable = roundForeground(drawable)
        super.setForegroundCompat(roundedDrawable)
        requestLayout()
        invalidate()
    }

    private fun roundForeground(drawable: Drawable?): Drawable? {
        var gradientDrawable: GradientDrawable? = null

        if (drawable is ColorDrawable) {
            val color = drawable.color
            gradientDrawable = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(color, color))
        } else if (drawable is GradientDrawable) {
            gradientDrawable = drawable.mutate() as GradientDrawable
        }

        if (gradientDrawable != null) {
            syncGradientDrawableRadii(gradientDrawable)
        }

        return gradientDrawable ?: drawable
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateBounds()
        invalidate()
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        updateBounds()
    }

    override fun setBackgroundResource(@DrawableRes resId: Int) {
        roundBackground(AppCompatResources.getDrawable(context, resId))
    }

    override fun setBackgroundDrawable(background: Drawable?) {
        roundBackground(background)
    }

    override fun setBackgroundColor(color: Int) {
        roundBackground(ColorDrawable(color))
    }

    override fun setBackground(background: Drawable?) {
        roundBackground(background)
    }

    private fun roundBackground(drawable: Drawable?) {
        if (!initialized) {
            return
        }

        backgroundDrawable = drawable
        backgroundDrawable?.setBounds(0, 0, width, height)

        when (drawable) {
            is BitmapDrawable -> updateValues(drawable)
            is ColorDrawable -> updateValues(drawable)
            is GradientDrawable -> updateValues(drawable)
            else -> reset()
        }
    }

    private fun updateBounds() {
        if (!initialized) {
            return
        }

        backgroundBounds.set(paddingLeft.toFloat(),
                paddingTop.toFloat(),
                (width - paddingRight).toFloat(),
                (height - paddingBottom).toFloat())

        if (backgroundDrawable is BitmapDrawable) {
            updateBitmapMatrix()
        } else {
            backgroundDrawable?.setBounds(0, 0, width, height)
        }

        updatePath()
    }

    private fun updateValues(drawable: ColorDrawable) {
        reset()
        backgroundPaint.color = drawable.color

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            backgroundPaint.colorFilter = drawable.colorFilter
        }

        updatePath()
    }

    private fun updateValues(drawable: GradientDrawable) {
        reset()
        syncGradientDrawableRadii(drawable)
    }

    private fun updateValues(drawable: BitmapDrawable) {
        val bitmap = drawable.bitmap
        reset()

        bitmapWidth = bitmap.width
        bitmapHeight = bitmap.height

        backgroundShader = BitmapShader(bitmap, drawable.tileModeX
                ?: Shader.TileMode.CLAMP, drawable.tileModeY ?: Shader.TileMode.CLAMP)

        updateBitmapMatrix()
        updatePath()

        backgroundPaint.colorFilter = drawable.paint.colorFilter
    }

    private fun updateBitmapMatrix() {
        if (backgroundShader == null) {
            return
        }

        var translationX = backgroundBounds.left
        var translationY = backgroundBounds.top
        val scale: Float

        if (bitmapWidth * backgroundBounds.height() > backgroundBounds.width() * bitmapHeight) {
            scale = backgroundBounds.height() / bitmapHeight
            translationX += (backgroundBounds.width() - bitmapWidth * scale) * 0.5f
        } else {
            scale = backgroundBounds.width() / bitmapWidth
            translationY += (backgroundBounds.height() - bitmapHeight * scale) * 0.5f
        }

        backgroundBitmapMatrix.reset()
        backgroundBitmapMatrix.setScale(scale, scale)
        backgroundBitmapMatrix.postTranslate(translationX, translationY)

        backgroundShader?.setLocalMatrix(backgroundBitmapMatrix)

        backgroundPaint.shader = backgroundShader
    }

    private fun updatePath() {
        val x1 = backgroundBounds.left
        val y1 = backgroundBounds.top
        val x2 = backgroundBounds.right
        val y2 = backgroundBounds.bottom

        backgroundPath.reset()
        backgroundPath.moveTo(0f, 0f)

        if (roundTopLeft) {
            backgroundPath.moveTo(x1 + radiusX, y1)
        } else {
            backgroundPath.moveTo(x1, y1)
        }

        if (roundTopRight) {
            backgroundPath.lineTo(x2 - radiusX, y1)
            backgroundPath.rQuadTo(radiusX, 0f, radiusX, radiusY)
        } else {
            backgroundPath.lineTo(x2, y1)
        }

        if (roundBottomRight) {
            backgroundPath.lineTo(x2, y2 - radiusY)
            backgroundPath.rQuadTo(0f, radiusY, -radiusX, radiusY)
        } else {
            backgroundPath.lineTo(x2, y2)
        }

        if (roundBottomLeft) {
            backgroundPath.lineTo(x1 + radiusX, y2)
            backgroundPath.rQuadTo(-radiusX, 0f, -radiusX, -radiusY)
        } else {
            backgroundPath.lineTo(x1, y2)
        }

        if (roundTopLeft) {
            backgroundPath.lineTo(x1, y1 + radiusY)
            backgroundPath.rQuadTo(0f, -radiusY, radiusX, -radiusY)
        } else {
            backgroundPath.lineTo(x1, y1)
        }

        backgroundPath.close()
    }

    private fun reset() {
        backgroundShader = null
        bitmapHeight = 0
        bitmapWidth = 0
        backgroundPaint.colorFilter = null
        backgroundPaint.shader = null
    }

    protected fun drawBackground(canvas: Canvas) {
        val drawable = backgroundDrawable

        if (drawable is BitmapDrawable || drawable is ColorDrawable) {
            canvas.drawPath(backgroundPath, backgroundPaint)
        } else {
            drawable?.draw(canvas)
        }
    }

    override fun onDraw(canvas: Canvas) {
        drawBackground(canvas)
        super.onDraw(canvas)
    }
}
