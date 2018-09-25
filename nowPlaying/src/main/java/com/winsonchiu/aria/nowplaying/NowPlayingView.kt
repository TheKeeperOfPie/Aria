package com.winsonchiu.aria.nowplaying

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnNextLayout
import com.squareup.picasso.Picasso
import com.winsonchiu.aria.artwork.ArtworkTransformation
import com.winsonchiu.aria.framework.util.initialize
import com.winsonchiu.aria.framework.view.ReflowAnimator
import kotlinx.android.synthetic.main.now_playing_content_view.view.*
import kotlinx.android.synthetic.main.now_playing_hidden_view.view.*
import kotlinx.android.synthetic.main.now_playing_view.view.*

class NowPlayingView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val artworkTransformation = ArtworkTransformation()

    private var progress = 0f

    init {
        initialize(R.layout.now_playing_view)
        loadLayoutDescription(R.xml.now_playing_view_scene)
        isClickable = true

        TypedValue().apply {
            context.theme.resolveAttribute(android.R.attr.windowBackground, this, true)
            setBackgroundColor(data)
        }

        doOnNextLayout {
            doOnNextLayout { (viewContent as MotionLayout).progress = 0f }
            requestLayout()
        }
    }

    fun setProgress(progress: Float) {
        this.progress = progress
        (viewContent as MotionLayout).progress = progress
        animatorTitle?.setCurrentFraction(1f - progress)
    }

    fun bindData(data: Model) {
        Picasso.get()
                .load(data.image)
                .transform(artworkTransformation)
                .into(viewContent.imageArtwork)

        viewHidden.layoutTitle.textTitleCollapsed.text = data.title
        viewHidden.layoutTitle.textTitleExpanded.text = data.title
        viewHidden.textDescriptionCollapsed.text = data.description
        viewHidden.layoutDescription.textDescriptionExpanded.text = data.description

        reflowAnimatorTitle?.reset()

        viewHidden.textTitleCollapsed.viewTreeObserver.addOnPreDrawListener(titlePreDrawListener)
    }

    private var reflowAnimatorTitle: ReflowAnimator? = null
    private var animatorTitle: ValueAnimator? = null

    private val titlePreDrawListener = object : ViewTreeObserver.OnPreDrawListener {
        @SuppressLint("RestrictedApi")
        override fun onPreDraw(): Boolean {
            val textStart = viewHidden.layoutTitle.textTitleCollapsed
            val textEnd = viewHidden.layoutTitle.textTitleExpanded

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
                    reflowAnimatorTitle = ReflowAnimator(textStart, textEnd, viewContent.viewSongTitle)
                    animatorTitle = reflowAnimatorTitle!!.createAnimator()

                    animatorTitle?.setCurrentFraction(progress)
                    textStart.viewTreeObserver.removeOnPreDrawListener(this)
                }
            } else {
                animatorTitle?.setCurrentFraction(progress)
                textStart.viewTreeObserver.removeOnPreDrawListener(this)
            }

            return true
        }
    }

    data class Model(
            val title: CharSequence?,
            val description: CharSequence?,
            val image: Uri?
    )
}