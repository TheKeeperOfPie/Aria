package com.winsonchiu.aria.nowplaying

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Outline
import android.graphics.drawable.BitmapDrawable
import android.media.audiofx.Visualizer
import android.net.Uri
import android.os.AsyncTask
import android.support.v4.media.session.PlaybackStateCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewOutlineProvider
import android.view.ViewPropertyAnimator
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.palette.graphics.Palette
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.winsonchiu.aria.artwork.ArtworkTransformation
import com.winsonchiu.aria.framework.animation.FirstLineTextAnimator
import com.winsonchiu.aria.framework.util.dpToPx
import com.winsonchiu.aria.framework.util.initialize
import com.winsonchiu.aria.media.MediaPlayer
import com.winsonchiu.aria.queue.QueueEntry
import kotlinx.android.synthetic.main.now_playing_content_view.view.*
import kotlinx.android.synthetic.main.now_playing_hidden_view.view.*
import kotlinx.android.synthetic.main.now_playing_view.view.*

class NowPlayingView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), CoordinatorLayout.AttachedBehavior {

    var listener: Listener? = null

    private val artworkTransformation = ArtworkTransformation()

    private var progress = 0f

    private val imageRadius = 4f.dpToPx(context)

    private val animatorTitle: FirstLineTextAnimator
    private val animatorDescription: FirstLineTextAnimator

    private var currentAudioSessionId = -1
    private var visualizer: Visualizer? = null

    private var isResumed = false

    private var paletteTask: AsyncTask<Bitmap, Void, Palette>? = null

    private var paletteListener = Palette.PaletteAsyncListener { palette -> viewContent?.viewWaveform?.setPalette(palette) }

    private val paletteCallback = object : Callback.EmptyCallback() {
        override fun onSuccess() {
            (imageArtwork?.drawable as? BitmapDrawable)?.bitmap?.let {
                paletteTask = Palette.from(it).generate(paletteListener)
            }
        }
    }

    init {
        initialize(R.layout.now_playing_view)
        loadLayoutDescription(R.xml.now_playing_view_scene)
        isClickable = true

        TypedValue().apply {
            context.theme.resolveAttribute(android.R.attr.windowBackground, this, true)
            setBackgroundColor(data)
        }

        animatorTitle = FirstLineTextAnimator(
                20.dpToPx(this),
                viewHidden.layoutTitle.textTitleExpanded,
                viewContent.viewSongTitle
        )

        animatorDescription = FirstLineTextAnimator(
                16.dpToPx(this),
                viewHidden.layoutDescription.textDescriptionExpanded,
                viewContent.viewSongDescription
        )

        imageSkipPrevious.setOnClickListener { listener?.onClickSkipPrevious() }
        imagePlay.setOnClickListener { listener?.onClickPlay() }
        imageSkipNext.setOnClickListener { listener?.onClickSkipNext() }

        viewContent.imageArtwork.clipToOutline = true
        viewContent.imageArtwork.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(
                    view: View,
                    outline: Outline
            ) {
                outline.setRoundRect(0, 0, view.width, view.height, imageRadius * progress)
            }
        }

        viewContent.viewWaveform.listener = object : AudioWaveformView.Listener {
            override fun onSeek(progress: Float) {
                listener?.onSeek(progress)
            }
        }

        setProgress(1f)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        paletteTask?.cancel(true)

        Picasso.get()
                .cancelRequest(imageArtwork)

        visualizer?.release()
        visualizer = null
    }

    fun setProgress(progress: Float) {
        this.progress = progress
        (viewContent as MotionLayout).progress = progress
        animatorTitle.setProgress(progress)
        animatorDescription.setProgress(progress)
    }

    fun setQueueEntry(queueEntry: QueueEntry) {
        viewContent.viewWaveform.setData(queueEntry)
    }

    fun bindData(data: Model) {
        Picasso.get()
                .load(data.image)
                .transform(artworkTransformation)
                .into(viewContent.imageArtwork, paletteCallback)

        viewHidden.layoutTitle.textTitleExpanded.text = data.title
        viewHidden.layoutDescription.textDescriptionExpanded.text = data.description
    }

    override fun getBehavior() = Behavior()

    fun setPlaybackState(playbackState: PlaybackStateCompat) {
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

        val resource = if (isPlaying) R.drawable.ic_pause_24dp else R.drawable.ic_play_arrow_24dp
        viewContent.imagePlay.setImageResource(resource)

        viewContent.viewWaveform.setPlaybackState(playbackState)

        val audioSessionId = playbackState.extras?.getInt(MediaPlayer.AUDIO_SESSION_ID, -1) ?: -1
        if (currentAudioSessionId != audioSessionId) {
            currentAudioSessionId = audioSessionId
            visualizer?.release()

//            try {
//                visualizer = Visualizer(audioSessionId)
//                visualizer?.let {
//                    it.captureSize = Visualizer.getCaptureSizeRange()[0]
//
//                    it.setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
//                        override fun onFftDataCapture(
//                                visualizer: Visualizer?,
//                                fft: ByteArray?,
//                                samplingRate: Int
//                        ) {
//                            Log.d("NowPlayingView", "onFftDataCapture called with ${fft?.contentToString()}")
//                        }
//
//                        override fun onWaveFormDataCapture(
//                                visualizer: Visualizer?,
//                                waveform: ByteArray?,
//                                samplingRate: Int
//                        ) {
//                            Log.d("NowPlayingView", "onWaveFormDataCapture called with ${waveform?.contentToString()}")
//                        }
//                    }, Visualizer.getMaxCaptureRate(), true, true)
//                }
//            } catch (e: Exception) {
//                if (BuildConfig.DEBUG) {
//                    throw e
//                }
//            }
        }

        if (isResumed) {
            visualizer?.enabled = true
        }
    }

    fun onResume() {
        isResumed = true
        visualizer?.enabled = true
    }

    fun onPause() {
        visualizer?.enabled = false
        isResumed = false
    }

    data class Model(
            val title: CharSequence?,
            val description: CharSequence?,
            val image: Uri?
    )

    interface Listener {
        fun onClickSkipPrevious()
        fun onClickPlay()
        fun onClickSkipNext()
        fun onSeek(progress: Float)
    }

    open class Behavior : BottomSheetBehavior<NowPlayingView> {

        companion object {
            private val LINEAR_OUT_SLOW_IN_INTERPOLATOR = LinearOutSlowInInterpolator()
            private val FAST_OUT_LINEAR_IN_INTERPOLATOR = FastOutSlowInInterpolator()
        }

        private val STATE_SCROLLED_DOWN = 1
        private val STATE_SCROLLED_UP = 2

        private var height = 0
        private var currentState = STATE_SCROLLED_UP
        private var currentAnimator: ViewPropertyAnimator? = null

        constructor() : super()

        constructor(
                context: Context,
                attrs: AttributeSet?
        ) : super(context, attrs)

        override fun onLayoutChild(
                parent: CoordinatorLayout,
                child: NowPlayingView,
                layoutDirection: Int
        ): Boolean {
            height = 72.dpToPx(child.context)
            return super.onLayoutChild(parent, child, layoutDirection)
        }

        override fun onStartNestedScroll(
                coordinatorLayout: CoordinatorLayout,
                child: NowPlayingView,
                directTargetChild: View,
                target: View,
                axes: Int,
                type: Int
        ): Boolean {
            return super.onStartNestedScroll(
                    coordinatorLayout,
                    child,
                    directTargetChild,
                    target,
                    axes,
                    type
            ) || axes == ViewCompat.SCROLL_AXIS_VERTICAL
        }

        override fun onNestedPreScroll(
                coordinatorLayout: CoordinatorLayout,
                child: NowPlayingView,
                target: View,
                dx: Int,
                dy: Int,
                consumed: IntArray,
                type: Int
        ) {
            if (currentState != STATE_SCROLLED_DOWN && dy > 0) {
                slideDown(child)
            } else if (currentState != STATE_SCROLLED_UP && dy < 0) {
                slideUp(child)
            }
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        }

        private fun slideUp(child: NowPlayingView) {
            currentAnimator?.cancel()
            child.clearAnimation()
            currentState = STATE_SCROLLED_UP
            animateChildTo(
                    child, 0, LINEAR_OUT_SLOW_IN_INTERPOLATOR
            )
        }

        /**
         * Perform an animation that will slide the child from it's current position to be totally off the
         * screen.
         */
        private fun slideDown(child: NowPlayingView) {
            currentAnimator?.cancel()
            child.clearAnimation()
            currentState = STATE_SCROLLED_DOWN
            animateChildTo(
                    child, height, FAST_OUT_LINEAR_IN_INTERPOLATOR
            )
        }

        private fun animateChildTo(
                child: NowPlayingView,
                targetY: Int,
                interpolator: TimeInterpolator
        ) {
            currentAnimator = child
                    .animate()
                    .translationY(targetY.toFloat())
                    .setInterpolator(interpolator)
                    .setListener(
                            object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    currentAnimator = null
                                }
                            })
        }
    }
}