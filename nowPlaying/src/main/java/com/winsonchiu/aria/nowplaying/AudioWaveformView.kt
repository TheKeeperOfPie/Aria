package com.winsonchiu.aria.nowplaying

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ComposeShader
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.RectF
import android.graphics.Shader
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.support.v4.media.session.PlaybackStateCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import androidx.annotation.WorkerThread
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import com.winsonchiu.aria.framework.util.dpToPx
import com.winsonchiu.aria.media.MediaPlayer
import com.winsonchiu.aria.queue.QueueEntry
import java.io.File
import java.io.FileInputStream
import kotlin.math.absoluteValue

class AudioWaveformView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val STEP_DIVISOR = 1000
        private const val HISTOGRAM_COUNT = 256
        private const val HISTOGRAM_MIN_THRESHOLD = 0.05f
        private const val HISTOGRAM_MAX_THRESHOLD = 0.01f
        private const val ENTRANCE_ANIMATION_DURATION = 200
        private const val EXIT_ANIMATION_DURATION = 200
        private const val FOREGROUND_ALPHA = 128
        private const val BACKGROUND_ALPHA = 128
    }

    var listener: Listener? = null

    private var extractor: MediaExtractor? = null
    private var codec: MediaCodec? = null

    private var basePathUp = Path()
    private var basePathDown = Path()

    private var sizedPathUp = Path()
    private var sizedPathDown = Path()

    private val pathMatrixUp = Matrix()
    private val pathMatrixDown = Matrix()

    private val pathBoundsUp = RectF()
    private val pathBoundsDown = RectF()

    private var shaderOne: LinearGradient? = null
    private var shaderTwo: LinearGradient? = null

    private val shaderMatrixOne = Matrix()
    private val shaderMatrixTwo = Matrix()

    private val paint = Paint()

    private var playbackUpdatePosition = 0
    private var playbackUpdateTime = 0L
    private var playbackDuration = 0

    private var progressOverride = -1f

    private var entranceAnimationStart = SystemClock.uptimeMillis()
    private val entranceAnimationMatrix = Matrix()

    private var exitAnimationStart = SystemClock.uptimeMillis()
    private val exitAnimationMatrix = Matrix()
    private var exitAnimationBasePath = Path()
    private var exitAnimationSizedPath = Path()

    private val animation = object : Animation() {
        override fun applyTransformation(
                interpolatedTime: Float,
                t: Transformation?
        ) {
            super.applyTransformation(interpolatedTime, t)
            invalidate()
        }
    }.apply {
        repeatCount = Animation.INFINITE
    }

    private var content: Uri? = null

    private val backgroundHandler: Handler
    private val backgroundHandlerThread = HandlerThread("AudioWaveformProcess")

    init {
        setWillNotDraw(false)

        backgroundHandlerThread.setUncaughtExceptionHandler { _, e ->
            e.printStackTrace()
        }
        backgroundHandlerThread.start()
        backgroundHandler = Handler(backgroundHandlerThread.looper)

        setPalette(null)
    }

    override fun onSizeChanged(
            width: Int,
            height: Int,
            oldWidth: Int,
            oldHeight: Int
    ) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        onChanged()
    }

    private fun onChanged() {
        if (basePathUp.isEmpty) {
            sizedPathUp.reset()
            sizedPathUp.addRect(0f, height.toFloat() - 8f.dpToPx(context), width.toFloat(), height.toFloat(), Path.Direction.CW)
        } else {
            basePathUp.computeBounds(pathBoundsUp, true)

            pathMatrixUp.setScale(
                    width / pathBoundsUp.right.coerceAtLeast(1f),
                    -height / pathBoundsUp.bottom.coerceAtLeast(1f)
            )
            pathMatrixUp.postTranslate(0f, height.toFloat())
            basePathUp.transform(pathMatrixUp, sizedPathUp)
        }

        invalidate()
    }

    fun setData(queueEntry: QueueEntry) {
        if (this.content == queueEntry.content) {
            return
        }

        this.content = queueEntry.content
        readSamples(File(File(queueEntry.content.path).absolutePath))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val eventX = event?.x ?: 0f
        val progress = (eventX / width).coerceIn(0f, 1f)

        when (event?.action) {
            MotionEvent.ACTION_CANCEL -> {
                progressOverride = -1f
            }
            MotionEvent.ACTION_UP -> {
                progressOverride = -1f
                playbackUpdatePosition = (playbackDuration * progress).toInt()
                playbackUpdateTime = SystemClock.elapsedRealtime()
                listener?.onSeek(progress)
                invalidate()
            }
            else -> {
                progressOverride = progress
            }
        }

        return true
    }

    fun setPlaybackState(playbackState: PlaybackStateCompat) {
        this.playbackUpdatePosition = playbackState.position.toInt()
        this.playbackUpdateTime = playbackState.lastPositionUpdateTime
        this.playbackDuration = playbackState.extras?.getInt(MediaPlayer.DURATION) ?: 1

        val isPlaying = when (playbackState.state) {
            PlaybackStateCompat.STATE_PLAYING,
            PlaybackStateCompat.STATE_FAST_FORWARDING,
            PlaybackStateCompat.STATE_REWINDING,
            PlaybackStateCompat.STATE_BUFFERING,
            PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS,
            PlaybackStateCompat.STATE_SKIPPING_TO_NEXT,
            PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM -> true
            PlaybackStateCompat.STATE_NONE,
            PlaybackStateCompat.STATE_STOPPED,
            PlaybackStateCompat.STATE_PAUSED,
            PlaybackStateCompat.STATE_ERROR,
            PlaybackStateCompat.STATE_CONNECTING -> false
            else -> false
        }

        if (isPlaying) {
            if (getAnimation() == null) {
                startAnimation(animation)
            }
        } else {
            clearAnimation()
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        when {
            progressOverride >= 0f -> updateShader(progressOverride)
            playbackUpdatePosition < 0 -> updateShader(0f)
            else -> {
                val currentPosition = playbackUpdatePosition + SystemClock.elapsedRealtime() - playbackUpdateTime
                val progress = (currentPosition / playbackDuration.toFloat()).coerceIn(0f, 1f)
                updateShader(progress)
            }
        }

        val entranceTimeDifference = SystemClock.uptimeMillis() - entranceAnimationStart

        if (entranceTimeDifference < ENTRANCE_ANIMATION_DURATION) {
            val progress = entranceTimeDifference.toFloat() / ENTRANCE_ANIMATION_DURATION
            entranceAnimationMatrix.setScale(1f, progress)
        } else {
            entranceAnimationMatrix.reset()
        }

        val exitTimeDifference = SystemClock.uptimeMillis() - exitAnimationStart

        if (exitTimeDifference < EXIT_ANIMATION_DURATION) {
            val progress = exitTimeDifference.toFloat() / EXIT_ANIMATION_DURATION
            exitAnimationMatrix.setScale(1f, 1f - progress)

            exitAnimationBasePath.transform(exitAnimationMatrix, exitAnimationSizedPath)
            exitAnimationSizedPath.transform(pathMatrixUp)

            canvas.drawPath(exitAnimationSizedPath, paint)
        } else {
            exitAnimationBasePath.reset()
            exitAnimationSizedPath.reset()
            exitAnimationMatrix.reset()
        }

        basePathUp.transform(entranceAnimationMatrix, sizedPathUp)
        sizedPathUp.transform(pathMatrixUp)

        canvas.drawPath(sizedPathUp, paint)

        super.onDraw(canvas)
    }

    @WorkerThread
    private fun release() {
        try {
            extractor?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            codec?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            codec?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        extractor = null
        codec = null
    }

    private fun readSamples(file: File) {
        backgroundHandler.postAtFrontOfQueue {
            release()

            if (!basePathUp.isEmpty && (exitAnimationStart + EXIT_ANIMATION_DURATION < SystemClock.uptimeMillis())) {
                exitAnimationBasePath.set(basePathUp)
                exitAnimationStart = SystemClock.uptimeMillis()
            }

            basePathUp.reset()
            onChanged()

            val extractor = MediaExtractor()
            var codec: MediaCodec? = null

            this.extractor = extractor

            try {
                FileInputStream(file).use {
                    extractor.setDataSource(it.fd)
                }

                val (index, format) = (0 until extractor.trackCount)
                        .asSequence()
                        .mapIndexed { index, it -> index to extractor.getTrackFormat(index) }
                        .find { (_, format) -> format.getString(MediaFormat.KEY_MIME).startsWith("audio") }
                        ?: return@postAtFrontOfQueue

                extractor.selectTrack(index)

                val channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

                val sampleArrayUp = mutableListOf<Double>()

                codec = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME))

                this.codec = codec

                codec.setCallback(object : MediaCodec.Callback() {
                    override fun onInputBufferAvailable(
                            codec: MediaCodec,
                            index: Int
                    ) {
                        val inputBuffer = codec.getInputBuffer(index)!!
                        val sampleSize = extractor.readSampleData(inputBuffer, 0)
                        extractor.advance()

                        if (sampleSize < 0) {
                            codec.queueInputBuffer(index, 0, 0, -1, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        } else {
                            val sampleTime = extractor.sampleTime
                            codec.queueInputBuffer(index, 0, sampleSize, sampleTime, 0)
                        }
                    }

                    override fun onOutputBufferAvailable(
                            codec: MediaCodec,
                            index: Int,
                            info: MediaCodec.BufferInfo
                    ) {
                        if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            post {
                                this@AudioWaveformView.basePathUp = generatePath(sampleArrayUp.toDoubleArray())
                                entranceAnimationStart = SystemClock.uptimeMillis()
                                onChanged()
                            }

                            codec.releaseOutputBuffer(index, false)

                            release()

                            if (isAttachedToWindow) {
                                postInvalidate()
                            }
                        } else {
                            val outputBuffer = codec.getOutputBuffer(index)!!

                            var upTotal = 0.0
                            var upCount = 0.0

                            var downTotal = 0.0
                            var downCount = 0.0

                            while (outputBuffer.hasRemaining()) {
                                val byte = outputBuffer.get()

                                if (byte > 0) {
                                    upTotal += byte
                                    upCount++
                                } else {
                                    downTotal += byte
                                    downCount++
                                }
                            }

                            val up = upTotal / upCount.coerceAtLeast(1.0) / channels
                            val down = downTotal.absoluteValue / downCount.coerceAtLeast(1.0) / channels

                            sampleArrayUp += up + down

                            codec.releaseOutputBuffer(index, false)
                        }
                    }

                    override fun onOutputFormatChanged(
                            codec: MediaCodec,
                            format: MediaFormat
                    ) {
                        // Ignored
                    }

                    override fun onError(
                            codec: MediaCodec,
                            e: MediaCodec.CodecException
                    ) {
                        // Ignored
                    }

                }, backgroundHandler)

                codec.configure(format, null, null, 0)
                codec.start()
            } catch (e: Exception) {
                codec?.release()
                this.codec = null

                extractor.release()
                this.extractor = null

                throw e
            }
        }
    }


    private fun generatePath(values: DoubleArray): Path {
        val size = values.size
        val absoluteMax = values.max() ?: 1.0

        val histogram = IntArray(HISTOGRAM_COUNT)
        values.forEach {
            val index = (it / absoluteMax * HISTOGRAM_COUNT).toInt().coerceIn(0, 255)
            histogram[index] = histogram[index] + 1
        }

        val minThreshold = size * HISTOGRAM_MIN_THRESHOLD
        var minAdjusted = 0.0
        var minSum = 0
        for ((index, value) in histogram.withIndex()) {
            minSum += value
            if (minSum > minThreshold) {
                minAdjusted = absoluteMax / HISTOGRAM_COUNT * index
                break
            }
        }

        val maxThreshold = size * HISTOGRAM_MAX_THRESHOLD
        var maxAdjusted = 0.0
        var maxSum = 0
        for (index in 255 downTo 0) {
            maxSum += histogram[index]
            if (maxSum > maxThreshold) {
                maxAdjusted = absoluteMax / HISTOGRAM_COUNT * index
                break
            }
        }

        val range = maxAdjusted - minAdjusted
        for (index in 0 until size) {
            val value = (values[index] - minAdjusted).coerceAtLeast(0.0) / range
            values[index] = value * value
        }

        val step = (size / STEP_DIVISOR).coerceAtLeast(1)

        val adjustedValues = values
                .asSequence()
                .windowed(step, step, true) { it.average() }
                .toList()
                .toDoubleArray()

        return pathFromValues(adjustedValues)
    }

    private fun pathFromValues(values: DoubleArray): Path {
        val path = Path()
        val size = values.size
        var index = 0

        if (size > 0) {
            values.asSequence()
                    .windowed(3, 3)
                    .forEach { window ->
                        val first = window.first()
                        val second = window.getOrElse(1) { window.first() }
                        val third = window.getOrElse(2) { window.first() }

                        path.cubicTo(
                                index++.toFloat(),
                                first.toFloat(),
                                index++.toFloat(),
                                second.toFloat(),
                                index++.toFloat(),
                                third.toFloat()
                        )
                    }

            path.cubicTo(index.toFloat(), 0f, index.toFloat(), 0f, index.toFloat(), 0f)
            path.close()
        }

        return path
    }

    fun setPalette(palette: Palette?) {
        val foreground = ColorUtils.setAlphaComponent(palette?.getLightVibrantColor(Color.WHITE) ?: Color.WHITE, FOREGROUND_ALPHA)
        val background = ColorUtils.setAlphaComponent(palette?.getMutedColor(Color.BLACK) ?: Color.BLACK, BACKGROUND_ALPHA)

        shaderOne = LinearGradient(
                0f,
                0f,
                1f,
                0f,
                intArrayOf(foreground, foreground, Color.TRANSPARENT),
                floatArrayOf(0f, 1f, Math.nextUp(1f)),
                Shader.TileMode.CLAMP
        )
        shaderTwo = LinearGradient(
                0f,
                0f,
                1f,
                0f,
                intArrayOf(Color.TRANSPARENT, background, background),
                floatArrayOf(0f, Math.nextUp(0f), 1f),
                Shader.TileMode.CLAMP
        )

        paint.shader = ComposeShader(shaderOne!!, shaderTwo!!, PorterDuff.Mode.SRC_OVER)
    }

    private fun updateShader(progress: Float) {
        shaderMatrixOne.setScale(progress * width, 1f)
        shaderOne?.setLocalMatrix(shaderMatrixOne)

        shaderMatrixTwo.setScale(width.toFloat(), 1f)
        shaderMatrixTwo.postTranslate(progress * width, 0f)
        shaderTwo?.setLocalMatrix(shaderMatrixTwo)
    }

    interface Listener {
        fun onSeek(progress: Float)
    }
}