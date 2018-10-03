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
import android.graphics.Rect
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
import android.view.TouchDelegate
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
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

    init {
        setWillNotDraw(false)
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

    private var playbackUpdatePosition = -1
    private var playbackUpdateTime = 0L
    private var playbackDuration = 0

    private var progressOverride = -1f

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

        val bounds = Rect(0, (-144).dpToPx(this), width, height + 72.dpToPx(this))

        touchDelegate = TouchDelegate(bounds, this)

        onChanged()
    }

    private fun onChanged() {
        basePathUp.computeBounds(pathBoundsUp, true)

        pathMatrixUp.setScale(
                width / pathBoundsUp.right.coerceAtLeast(1f),
                -height / pathBoundsUp.bottom.coerceAtLeast(1f)
        )
        pathMatrixUp.postTranslate(0f, height.toFloat())
        basePathUp.transform(pathMatrixUp, sizedPathUp)

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

        canvas.drawPath(sizedPathUp, paint)

        super.onDraw(canvas)
    }

    private fun readSamples(file: File) {
        backgroundHandler.postAtFrontOfQueue {
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
                val sampleArrayDown = mutableListOf<Double>()

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
                                onChanged()
                            }

                            codec.releaseOutputBuffer(index, false)
                            codec.release()
                            extractor.release()

                            if (isAttachedToWindow) {
                                postInvalidate()
                            }
                        } else {
                            val outputBuffer = codec.getOutputBuffer(index)!!

                            var upTotal = 0L
                            var upCount = 0L

                            var downTotal = 0
                            var downCount = 0

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

                            sampleArrayUp += upTotal / upCount.coerceAtLeast(1).toDouble()//Math.sqrt(upTotal / upCount.coerceAtLeast(1).toDouble() / channels)
                            sampleArrayDown += Math
                                    .sqrt(downTotal.absoluteValue / downCount.coerceAtLeast(1).toDouble() / channels)

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

        val histogram = IntArray(256)
        values.forEach {
            val index = (it / absoluteMax * 256).toInt().coerceIn(0, 255)
            histogram[index] = histogram[index] + 1
        }

        val minThreshold = size / 20
        var minAdjusted = 0.0
        var minSum = 0
        for ((index, value) in histogram.withIndex()) {
            minSum += value
            if (minSum > minThreshold) {
                minAdjusted = absoluteMax / 256f * index
                break
            }
        }

        val maxThreshold = size / 100
        var maxAdjusted = 0.0
        var maxSum = 0
        for (index in 255 downTo 0) {
            maxSum += histogram[index]
            if (maxSum > maxThreshold) {
                maxAdjusted = absoluteMax / 256f * index
                break
            }
        }

        val range = maxAdjusted - minAdjusted
        for (index in 0 until size) {
            val value = (values[index] - minAdjusted).coerceAtLeast(0.0) / range
            values[index] = value * value
        }

        val step = size / 100

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
        val foreground = ColorUtils.setAlphaComponent(palette?.getLightVibrantColor(Color.WHITE) ?: Color.WHITE, 232)
        val background = ColorUtils.setAlphaComponent(palette?.getMutedColor(Color.BLACK) ?: Color.BLACK, 232)

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