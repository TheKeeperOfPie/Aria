package com.winsonchiu.aria.nowplaying

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.content.Context
import android.graphics.Outline
import android.net.Uri
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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.squareup.picasso.Picasso
import com.winsonchiu.aria.artwork.ArtworkTransformation
import com.winsonchiu.aria.framework.animation.FirstLineTextAnimator
import com.winsonchiu.aria.framework.util.dpToPx
import com.winsonchiu.aria.framework.util.initialize
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

        setProgress(1f)
    }

    fun setProgress(progress: Float) {
        this.progress = progress
        (viewContent as MotionLayout).progress = progress
        animatorTitle.setProgress(progress)
        animatorDescription.setProgress(progress)
    }

    fun bindData(data: Model) {
        Picasso.get()
                .load(data.image)
                .transform(artworkTransformation)
                .into(viewContent.imageArtwork)

        viewHidden.layoutTitle.textTitleExpanded.text = data.title
        viewHidden.layoutDescription.textDescriptionExpanded.text = data.description
    }

    override fun getBehavior() = Behavior()

    data class Model(
            val title: CharSequence?,
            val description: CharSequence?,
            val image: Uri?
    )

    interface Listener {
        fun onClickSkipPrevious()
        fun onClickPlay()
        fun onClickSkipNext()
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
            return super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type) || axes == ViewCompat.SCROLL_AXIS_VERTICAL
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