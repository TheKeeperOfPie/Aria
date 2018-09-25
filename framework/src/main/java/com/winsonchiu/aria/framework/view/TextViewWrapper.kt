package com.winsonchiu.aria.framework.view

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.util.Property
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isInvisible
import androidx.transition.PathMotion
import com.winsonchiu.aria.framework.util.animation.AnimationUtils

class TextViewWrapper @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    lateinit var textStart: ReflowTextView
    lateinit var textEnd: TextView

    override fun onFinishInflate() {
        super.onFinishInflate()

        textStart = getChildAt(0) as ReflowTextView
        textEnd = getChildAt(1) as TextView
        textEnd.isInvisible = !isInEditMode
    }

    var text: CharSequence?
        get() = textStart.text
        set(value) {
            textStart.text = value
            textEnd.text = value

            reflowAnimator?.reset()

            textStart.viewTreeObserver.addOnPreDrawListener(titlePreDrawListener)
        }

    var progress
        get() = animator?.animatedFraction ?: 0f
        set(value) {
            animator?.setCurrentFraction(value)
        }

    private var reflowAnimator: ReflowAnimator? = null
    private var animator: ValueAnimator? = null

    private val titlePreDrawListener = object : ViewTreeObserver.OnPreDrawListener {
        @SuppressLint("RestrictedApi")
        override fun onPreDraw(): Boolean {
            if (textStart.visibility != View.GONE && textStart.text.isNotEmpty()) {
                val startLaidOut = textStart.isLaidOut
                val endLaidOut = textEnd.isLaidOut
                val startWidthNonZero = textStart.measuredWidth > 0
                val startHeightNonZero = textStart.measuredHeight > 0
                val endWidthNonZero = textEnd.measuredWidth > 0
                val endHeightNonZero = textEnd.measuredHeight > 0

                if (startLaidOut
                        && endLaidOut
                        && startWidthNonZero
                        && startHeightNonZero
                        && endWidthNonZero
                        && endHeightNonZero) {
//                    reflowAnimator = ReflowAnimator(textStart, textEnd)
                    animator = reflowAnimator!!.createAnimator()

                    updateProgress()
                    textStart.viewTreeObserver.removeOnPreDrawListener(this)
                }
            } else {
                updateProgress()
                textStart.viewTreeObserver.removeOnPreDrawListener(this)
            }

            return true
        }
    }

    fun updateProgress() {
        animator?.setCurrentFraction(progress)
    }
}

class SwitchDrawable(
        private var startBitmap: Bitmap,
        private val startBitmapSrcBounds: Rect,
        private val startFontSize: Float,
        private val endBitmap: Bitmap,
        private var endBitmapSrcBounds: Rect,
        private val endFontSize: Float,
        textPaint: Paint?
) : Drawable() {

    init {
        Log.d("SwitchDrawable", "endBitmapSrcBounds = $endBitmapSrcBounds")
        Log.d("SwitchDrawable", "endBitmap = $endBitmap")
//        endBitmapSrcBounds = Rect(0, 0, endBitmap.width, endBitmap.height)
    }

    companion object {
        private const val ELLIPSIS = "…"

        val TOP_LEFT: Property<SwitchDrawable, PointF> = object : Property<SwitchDrawable, PointF>(
                PointF::class.java,
                "topLeft"
        ) {
            override fun set(
                    drawable: SwitchDrawable,
                    topLeft: PointF
            ) {
                drawable.topLeft = topLeft
            }

            override fun get(drawable: SwitchDrawable): PointF? {
                return drawable.topLeft
            }
        }

        val WIDTH: Property<SwitchDrawable, Int> = object : Property<SwitchDrawable, Int>(Int::class.java, "width") {
            override fun set(
                    drawable: SwitchDrawable,
                    width: Int
            ) {
                drawable.width = width
            }

            override fun get(drawable: SwitchDrawable): Int {
                return drawable.width
            }
        }

        val HEIGHT: Property<SwitchDrawable, Int> = object : Property<SwitchDrawable, Int>(Int::class.java, "height") {
            override fun set(
                    drawable: SwitchDrawable,
                    height: Int
            ) {
                drawable.height = height
            }

            override fun get(drawable: SwitchDrawable): Int {
                return drawable.height
            }
        }

        val ALPHA: Property<SwitchDrawable, Int> = object : Property<SwitchDrawable, Int>(Int::class.java, "alpha") {
            override fun set(
                    drawable: SwitchDrawable,
                    alpha: Int?
            ) {
                drawable.alpha = alpha!!
            }

            override fun get(drawable: SwitchDrawable): Int {
                return DrawableCompat.getAlpha(drawable)
            }
        }

        val PROGRESS: Property<SwitchDrawable, Float> = object : Property<SwitchDrawable, Float>(
                Float::class.java,
                "progress"
        ) {
            override fun set(
                    drawable: SwitchDrawable,
                    progress: Float?
            ) {
                drawable.setProgress(progress!!)
            }

            override fun get(drawable: SwitchDrawable): Float {
                return 0f
            }
        }
    }

    private val textPaint = textPaint?.let { TextPaint(it) }
    private val textColorStart = textPaint?.color
    private val textSizeStart = textPaint?.textSize

    private var currentBitmap: Bitmap? = startBitmap
    private var currentBitmapSrcBounds: Rect? = startBitmapSrcBounds

    private val paint: Paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)
    private val switchThreshold: Float = startFontSize / (startFontSize + endFontSize)

    private var progress = 0f

    var topLeft: PointF? = null
        set(topLeft) {
            field = topLeft
//            Log.d("SwitchDrawable", "topLeft called with $topLeft")
            updateBounds()
        }
    var width: Int = 0
        set(width) {
            field = width
//            Log.d("SwitchDrawable", "topLeft called with $width")
            updateBounds()
        }
    var height: Int = 0
        set(height) {
            field = height
//            Log.d("SwitchDrawable", "topLeft called with $height")
            updateBounds()
        }

    override fun draw(canvas: Canvas) {
        canvas.drawBitmap(currentBitmap!!, currentBitmapSrcBounds, bounds, paint)

        if (textPaint != null) {
            textPaint.color = ColorUtils.setAlphaComponent(textColorStart!!, (progress * 255).toInt())
            textPaint.textSize = AnimationUtils
                    .lerp(
                            textSizeStart!!,
                            textSizeStart * (startBitmapSrcBounds.height() / endBitmapSrcBounds.height()),
                            progress
                    )
            canvas.drawText(ELLIPSIS, bounds.right.toFloat(), bounds.bottom.toFloat() - textPaint.descent(), textPaint)
        }
    }

    override fun getAlpha(): Int {
        return paint.alpha
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun getColorFilter(): ColorFilter? {
        return paint.colorFilter
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    fun setProgress(progress: Float) {
        this.progress = progress

        if (progress >= switchThreshold) {
            currentBitmap = endBitmap
            currentBitmapSrcBounds = endBitmapSrcBounds
        } else {
            currentBitmap = startBitmap
            currentBitmapSrcBounds = startBitmapSrcBounds
        }

        invalidateSelf()
    }

    private fun updateBounds() {
        val topLeft = topLeft
        val left = topLeft?.x?.let { Math.round(it) } ?: 0
        val top = topLeft?.y?.let { Math.round(it) } ?: 0
        setBounds(left, top, left + this.width, top + this.height)
    }

    fun copy() = SwitchDrawable(
            startBitmap,
            Rect(startBitmapSrcBounds),
            startFontSize,
            endBitmap,
            Rect(endBitmapSrcBounds),
            endFontSize,
            textPaint?.let { TextPaint(it) }
    ).also {
        it.topLeft = topLeft
        it.width = width
        it.height = height
        it.alpha = alpha
        it.bounds = bounds
        it.colorFilter = colorFilter
        it.setProgress(progress)
    }
}

class ReflowTextView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private val switchCallback = object : Drawable.Callback {
        override fun unscheduleDrawable(
                who: Drawable,
                what: Runnable
        ) {
            invalidate()
        }

        override fun scheduleDrawable(
                who: Drawable,
                what: Runnable,
                `when`: Long
        ) {
            invalidate()
        }

        override fun invalidateDrawable(who: Drawable) {
            invalidate()
        }
    }

    private var switchDrawables = ArrayList<Drawable>()

    var disableDrawForReflow: Boolean = false

    fun addSwitchDrawable(drawable: Drawable) {
        drawable.callback = switchCallback
        switchDrawables.add(drawable)
    }

    fun clearSwitchDrawables() {
        switchDrawables.clear()
    }

    fun getSwitchDrawables() = switchDrawables as List<Drawable>

    override fun dispatchDraw(canvas: Canvas) {
        if (disableDrawForReflow && switchDrawables.isNotEmpty()) {
            switchDrawables.forEach { it.draw(canvas) }
        } else {
            super.dispatchDraw(canvas)
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (!disableDrawForReflow || switchDrawables.isEmpty()) {
            super.onDraw(canvas)
        }
    }
}

class ReflowDrawView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val switchCallback = object : Drawable.Callback {
        override fun unscheduleDrawable(
                who: Drawable,
                what: Runnable
        ) {
            invalidate()
        }

        override fun scheduleDrawable(
                who: Drawable,
                what: Runnable,
                `when`: Long
        ) {
            invalidate()
        }

        override fun invalidateDrawable(who: Drawable) {
            invalidate()
        }
    }

    private var switchDrawables = mutableListOf<Drawable>()

    fun addSwitchDrawable(drawable: Drawable) {
        drawable.callback = switchCallback
        switchDrawables.add(drawable)
    }

    fun clearSwitchDrawables() {
        switchDrawables.clear()
    }

    override fun dispatchDraw(canvas: Canvas) {
        switchDrawables.forEach { it.draw(canvas) }
    }
}

/**
 * This is copied from https://github.com/shazam/reflow-animator, which is derived from
 * https://github.com/nickbutcher/plaid, and modified specifically for use inside [SlantHeaderView]
 *
 * A transition for repositioning text. This will animate changes in text size and position,
 * re-flowing line breaks as necessary.
 *
 *
 * Strongly recommended to use a curved `pathMotion` for a more natural transition.
 */
class ReflowAnimator constructor(
        private val sourceView: TextView,
        private val targetView: TextView,
        private val drawView: ReflowDrawView
) {

    companion object {
        private val TRANSPARENT = 0
        private val OPAQUE = 255
        private val OPACITY_MID_TRANSITION = (0.8f * OPAQUE).toInt()
        val ELLIPSIS = '…'

        // setCurrentFraction is only >22, so use a long duration and setCurrentPlayTime
        val DURATION = 10000f

        private fun getBounds(view: View): Rect {
            val loc = IntArray(2)
            view.getLocationInWindow(loc)
            return Rect(loc[0], loc[1], loc[0] + view.width, loc[1] + view.height)
        }

        private fun getSectionWidth(
                layout: Layout,
                sectionStart: Int,
                sectionEnd: Int
        ): Int {
            val text = layout.text
            val paint = TextPaint()
            paint.set(layout.paint)

            return Layout.getDesiredWidth(text, sectionStart, sectionEnd, paint).toInt()
        }

        private fun createUnrestrictedLayout(view: TextView): Layout {
            val text = view.text
            val layout = view.layout
            val paint = TextPaint()
            paint.set(layout.paint)

            return if (SDK_INT >= M) {
                StaticLayout.Builder
                        .obtain(text, 0, text.length, paint, layout.width)
                        .setAlignment(layout.alignment)
                        .setLineSpacing(view.lineSpacingExtra, view.lineSpacingMultiplier)
                        .setIncludePad(view.includeFontPadding)
                        .setBreakStrategy(view.breakStrategy)
                        .setHyphenationFrequency(view.hyphenationFrequency)
                        .build()
            } else {
                StaticLayout(
                        text,
                        paint,
                        text.length,
                        layout.alignment,
                        view.lineSpacingMultiplier,
                        view.lineSpacingExtra,
                        view.includeFontPadding
                )
            }
        }

        private fun getPath(
                startX: Float,
                startY: Float,
                endX: Float,
                endY: Float
        ): Path {
            val path = Path()
            path.moveTo(startX, startY)
            path.lineTo(endX, endY)
            return path
        }
    }

    private val animator = ValueAnimator.ofFloat(0f, 1f)
    private var startText: Bitmap? = null
    private var endText: Bitmap? = null

    /**
     * Create an animator to transform between `from` and `to`, using the configuration defined in the builder.
     * @return An Android Animator. Run or add in an AnimatorSet.
     */
    fun createAnimator(): ValueAnimator {
        reset()

        Log.d("TextViewWrapper", "createAnimator called with")

        // capture bitmaps of the text
        startText = createBitmap(sourceView)
        endText = createBitmap(targetView)

        // temporarily turn off clipping so we can draw outside of our bounds don't draw
        (sourceView.parent as ViewGroup).clipChildren = false

        // calculate the runs of text to move together
        val runs = runs

        // buildAnimator animators for moving, scaling and fading each run of text
        val runAnimators = createRunAnimators(sourceView, drawView, startText!!, endText!!, runs)
        runAnimators.forEach { it.duration = DURATION.toLong() }

        animator.addUpdateListener {
            val animatedFraction = it.animatedFraction
            runAnimators.forEach { runAnimator -> runAnimator.currentPlayTime = (animatedFraction * DURATION).toLong() }
        }

        animator.duration = DURATION.toLong()

        return animator
    }

    /**
     * Call when text has changed to reset drawables
     */
    fun reset() {
        drawView.clearSwitchDrawables()
    }

    /**
     * Call to recycle bitmaps
     */
    fun destroy() {
        startText?.recycle()
        startText = null
        endText?.recycle()
        endText = null
    }

    /**
     * Calculate the [Run]s i.e. diff the start and end states, see where text changes
     * line and track the bounds of sections of text that can move together.
     *
     *
     * If a text block has a max number of lines, consider both with and without this limit applied.
     * This allows simulating the correct line breaking as well as calculating the position that
     * overflowing text would have been laid out, so that it can animate from/to that position.
     */
    private // work out which line this letter is on in the start state
    // buildAnimator a fake Run to hide '...'
    // work out which line this letter is on in the end state
    // at a run boundary, store bounds in both states
    val runs: List<ReflowRun>
        get() {
            val textLength = Math.max(
                    sourceView.layout.getLineVisibleEnd(sourceView.layout.lineCount - 1),
                    targetView.layout.getLineVisibleEnd(targetView.layout.lineCount - 1)
            )
            var currentStartLine = 0
            var currentStartRunLeft = 0
            var currentStartRunTop = 0
            var currentEndLine = 0
            var currentEndRunLeft = 0
            var currentEndRunTop = 0
            val runs = ArrayList<ReflowRun>()

            val startLayout = sourceView.layout
            val endLayout = targetView.layout

            var startOffsetLeft = -1
            var endOffsetLeft = -1

            var lastCharPosition = 0
            var charPosition = 0
            while (charPosition < textLength) {
                val isLastChar = charPosition == textLength - 1
                val startLine = startLayout.getLineForOffset(charPosition)
                val endLine = endLayout.getLineForOffset(charPosition)

                if (startLine != currentStartLine
                        || endLine != currentEndLine
                        || isLastChar) {
                    if (isLastChar) {
                        charPosition += 1
                    }

                    currentStartLine = Math.min(currentStartLine, startLayout.lineCount - 1)
                    currentEndLine = Math.min(currentEndLine, endLayout.lineCount - 1)
                    val startRunBottom = startLayout.getLineBottom(currentStartLine)
                    val endRunBottom = endLayout.getLineBottom(currentEndLine)

                    if (currentStartLine == 0 && startOffsetLeft == -1) {
                        startOffsetLeft = getStartOffsetLeft(startLayout, currentStartLine)
                    }
                    if (currentEndLine == 0 && endOffsetLeft == -1) {
                        endOffsetLeft = getStartOffsetLeft(endLayout, currentEndLine)
                    }

                    val startBound = Rect(
                            currentStartRunLeft,
                            currentStartRunTop,
                            currentStartRunLeft + getSectionWidth(startLayout, lastCharPosition, charPosition),
                            startRunBottom
                    )
                    startBound.offset(sourceView.compoundPaddingLeft + startOffsetLeft, sourceView.compoundPaddingTop)
                    val endBound = Rect(
                            currentEndRunLeft,
                            currentEndRunTop,
                            currentEndRunLeft + getSectionWidth(endLayout, lastCharPosition, charPosition),
                            endRunBottom
                    )
                    endBound.offset(targetView.compoundPaddingLeft + endOffsetLeft, targetView.compoundPaddingTop)
                    val isStartVisible = startRunBottom <= sourceView.measuredHeight
                    val isEndVisible = endRunBottom <= targetView.measuredHeight
                    if (isStartVisible || isEndVisible) {
                        runs.add(
                                ReflowRun(
                                        startBound,
                                        isStartVisible,
                                        endBound,
                                        isEndVisible
                                )
                        )
                    } else {
                        break
                    }
                    currentStartLine = startLine
                    currentStartRunTop = startLayout.getLineTop(startLine)
                    currentEndLine = endLine
                    currentEndRunTop = endLayout.getLineTop(endLine)

                    // An obscure paint measuring exception can be thrown here, but we can ignore
                    try {
                        val endLeft = endLayout.getPrimaryHorizontal(charPosition).toInt()
                        val startLeft = startLayout.getPrimaryHorizontal(charPosition).toInt()
                        currentStartRunLeft = startLeft
                        currentEndRunLeft = endLeft
                    } catch (ignored: Exception) {
                    }

                    startOffsetLeft = 0
                    endOffsetLeft = 0
                    lastCharPosition = charPosition
                }
                charPosition++
            }
            return runs
        }

    private fun getStartOffsetLeft(
            startLayout: Layout,
            currentStartLine: Int
    ): Int {
        return startLayout.getLineLeft(currentStartLine).toInt()
    }

    /**
     * Create Animators to transition each run of text from start to end position and size.
     */
    private fun createRunAnimators(
            view: TextView,
            drawView: ReflowDrawView,
            startText: Bitmap,
            endText: Bitmap,
            runs: List<ReflowRun>
    ): List<ValueAnimator> {
        val sourceViewBounds = getBounds(sourceView) // position on the screen of source view
        val targetViewBounds = getBounds(targetView) // position on the screen of target view

        val animators = ArrayList<ValueAnimator>(runs.size)
        val dx = sourceViewBounds.left - targetViewBounds.left
        val dy = sourceViewBounds.top - targetViewBounds.top
        // move text closest to the destination first i.e. loop forward or backward over the runs
        val upward = sourceViewBounds.centerY() > targetViewBounds.centerY()
        val linearInterpolator = LinearInterpolator()

        var i = if (upward) 0 else runs.size - 1
        while (upward && i < runs.size || !upward && i >= 0) {
            val run = runs[i]

            // skip text runs which aren't visible in either state
            if (!run.isStartVisible && !run.isEndVisible) {
                i += if (upward) 1 else -1
                continue
            }

            // buildAnimator & position the drawable which displays the run; add it to the overlay.
            var paint: TextPaint? = null
            if (i == 0 && runs.size > 1) {
                paint = TextPaint()
                paint.set(targetView.paint)
            }

            val drawable = SwitchDrawable(
                    startText, run.start, sourceView.textSize,
                    endText, run.end, targetView.textSize, paint
            )
            drawable.setBounds(
                    run.start.left,
                    run.start.top,
                    run.start.right,
                    run.start.bottom
            )
            drawView.addSwitchDrawable(drawable)

            val topLeft = getPathValuesHolder(run, dy, dx, false)
            val width = PropertyValuesHolder.ofInt(
                    SwitchDrawable.WIDTH, run.start.width(), run.end.width()
            )
            val height = PropertyValuesHolder.ofInt(
                    SwitchDrawable.HEIGHT, run.start.height(), run.end.height()
            )
            // the progress property drives the switching behaviour
            val progress = PropertyValuesHolder.ofFloat(
                    SwitchDrawable.PROGRESS, 0f, 1f
            )
            val runAnim = if (i == -1/*0*/) ObjectAnimator.ofPropertyValuesHolder(
                    drawable, topLeft, width, height, progress
            ) else
                ObjectAnimator.ofPropertyValuesHolder(
                        drawable, getPathValuesHolder(run, dy, dx, true), width, height, progress
                )

            animators.add(runAnim)

            if (/*i != 0 || */(run.isStartVisible != run.isEndVisible)) {
                // if run is appearing/disappearing then fade it in/out
                val fade = ValueAnimator.ofFloat(0f, 1f)
                val startAlpha = if (run.isStartVisible) 255 else 0
                val endAlpha = if (run.isEndVisible && i == 0) 255 else 0

                fade.addUpdateListener {
                    val adjustedPercentage = AnimationUtils.shiftRange(0f, 0.5f, it.animatedFraction)
                    drawable.alpha = AnimationUtils.lerp(startAlpha, endAlpha, adjustedPercentage)
                }
                if (!run.isStartVisible) {
                    drawable.alpha = TRANSPARENT
                }
                animators.add(fade)
            } else {
                // slightly fade during transition to minimize movement
                val fade = ObjectAnimator.ofInt(
                        drawable,
                        SwitchDrawable.ALPHA,
                        OPAQUE, OPACITY_MID_TRANSITION, OPAQUE
                )
                fade.interpolator = linearInterpolator
                animators.add(fade)
            }
            i += if (upward) 1 else -1
        }
        return animators
    }

    private fun getPathValuesHolder(
            run: ReflowRun,
            dy: Int,
            dx: Int,
            skipX: Boolean
    ): PropertyValuesHolder {
        val propertyValuesHolder: PropertyValuesHolder
        val pathMotion = object : PathMotion() {
            override fun getPath(
                    startX: Float,
                    startY: Float,
                    endX: Float,
                    endY: Float
            ): Path {
                return ReflowAnimator.getPath(startX, startY, endX, endY)
            }
        }
        propertyValuesHolder = PropertyValuesHolder.ofObject<PointF>(
                SwitchDrawable.TOP_LEFT, null,
                pathMotion.getPath(
                        run.start.left.toFloat(),
                        run.start.top.toFloat(),
                        if (skipX) run.start.left.toFloat() else (run.end.left - dx).toFloat(),
                        (run.end.top - dy).toFloat()
                )
        )

        return propertyValuesHolder
    }

    private fun createBitmap(view: View): Bitmap {
        val width = view.measuredWidth
        val height = view.measuredHeight
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
}

data class ReflowRun(
        val start: Rect,
        val isStartVisible: Boolean,
        val end: Rect,
        val isEndVisible: Boolean
)