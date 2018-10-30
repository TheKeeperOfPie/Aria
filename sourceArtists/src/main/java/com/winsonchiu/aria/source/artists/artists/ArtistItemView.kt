package com.winsonchiu.aria.source.artists.artists

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.RippleDrawable
import android.os.AsyncTask
import android.util.AttributeSet
import android.view.animation.Animation
import android.view.animation.Transformation
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.palette.graphics.Palette
import com.airbnb.epoxy.AfterPropsSet
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.winsonchiu.aria.framework.util.ColorUtils
import com.winsonchiu.aria.framework.util.RoundedOutlineProvider
import com.winsonchiu.aria.framework.util.dpToPx
import com.winsonchiu.aria.framework.util.initialize
import com.winsonchiu.aria.framework.util.mostPopulous
import com.winsonchiu.aria.framework.util.multiplyValue
import com.winsonchiu.aria.framework.util.withAlpha
import com.winsonchiu.aria.framework.util.withMaxAlpha
import com.winsonchiu.aria.source.artists.Artist
import com.winsonchiu.aria.source.artists.ArtistsUtils
import com.winsonchiu.aria.source.artists.R
import com.winsonchiu.aria.source.artists.transition.ArtistsToArtistTransition
import kotlinx.android.synthetic.main.artist_item_view.view.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ArtistItemView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = -1
) : ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        private val INTERPOLATOR = FastOutSlowInInterpolator()
    }

    @set:ModelProp
    lateinit var artist: Artist

    var lastArtist: Artist? = null

    @set:ModelProp(ModelProp.Option.DoNotHash)
    var listener: Listener? = null

    var paletteBackgroundColor = Color.TRANSPARENT

    private val drawable = RippleDrawable(ColorStateList.valueOf(Color.WHITE), null, null)

    private var paletteTask: AsyncTask<Bitmap, Void, Palette>? = null

    private var paletteListener = Palette
            .PaletteAsyncListener {
                it?.mostPopulous()?.let {
                    paletteBackgroundColor = it.rgb
                    drawable.setColor(ColorStateList.valueOf(paletteBackgroundColor.multiplyValue(1.2f)))
                    clearAnimation()
                    startAnimation(PaletteResultAnimation(it))
                }
            }

    private val paletteCallback = object : Callback.EmptyCallback() {
        override fun onSuccess() {
            (image.drawable as? BitmapDrawable)?.bitmap?.let {
                paletteTask = Palette.from(it).generate(paletteListener)
            }
        }
    }

    init {
        initialize(R.layout.artist_item_view)

        foreground = drawable
        isTransitionGroup = true

        outlineProvider = RoundedOutlineProvider(4f.dpToPx(context))
        clipToOutline = true

        setOnClickListener { listener?.onClick(this, artist) }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        paletteTask?.cancel(true)
        Picasso.get().cancelRequest(image)
    }

    @AfterPropsSet
    fun onChanged() {
        val changed = this.lastArtist?.id != artist.id
        if (changed) {
            setBackgroundColor(Color.TRANSPARENT)
            textName.setTextColor(Color.WHITE)
        }

        clearAnimation()

        this.lastArtist = artist

        transitionName = ArtistsToArtistTransition.header(artist.id)
        image.transitionName = ArtistsToArtistTransition.image(artist.id)
        image.alpha = 1f
        image.isVisible = true

        textName.text = artist.name

        val spanCount = ArtistsUtils.spanCount(context)
        val targetWidth = resources.displayMetrics.widthPixels / spanCount

        Picasso.get()
                .load(artist.image)
                .resize(targetWidth, 0)
                .onlyScaleDown()
//                .transform(ArtworkTransformation(targetWidth = targetWidth))
                .into(image, paletteCallback)

        Picasso.get()
                .load(artist.image)
                .fetch()
    }

    private inner class PaletteResultAnimation(
            swatch: Palette.Swatch
    ) : Animation() {

        private val finalBackgroundColor = swatch.rgb
        private val finalTextColor = swatch.bodyTextColor.withMaxAlpha()

        init {
            duration = 200
            interpolator = INTERPOLATOR
        }

        override fun applyTransformation(
                interpolatedTime: Float,
                t: Transformation?
        ) {
            super.applyTransformation(interpolatedTime, t)

            this@ArtistItemView.setBackgroundColor(finalBackgroundColor.withAlpha(interpolatedTime))

            textName.setTextColor(
                    ColorUtils.crossFadeOver(
                            Color.WHITE,
                            finalTextColor,
                            interpolatedTime
                    )
            )

            invalidate()
        }

        override fun willChangeTransformationMatrix() = false
    }

    interface Listener {
        fun onClick(view: ArtistItemView, artist: Artist)
    }
}