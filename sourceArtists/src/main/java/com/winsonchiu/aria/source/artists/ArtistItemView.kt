package com.winsonchiu.aria.source.artists

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.util.AttributeSet
import android.view.animation.Animation
import android.view.animation.Transformation
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.palette.graphics.Palette
import com.airbnb.epoxy.AfterPropsSet
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.winsonchiu.aria.artwork.ArtworkTransformation
import com.winsonchiu.aria.framework.util.ColorUtils
import com.winsonchiu.aria.framework.util.dpToPx
import com.winsonchiu.aria.framework.util.initialize
import com.winsonchiu.aria.framework.util.mostPopulous
import com.winsonchiu.aria.framework.util.withAlpha
import com.winsonchiu.aria.framework.util.withMaxAlpha
import kotlinx.android.synthetic.main.artist_item_view.view.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ArtistItemView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = com.google.android.material.R.attr.materialCardViewStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private val INTERPOLATOR = FastOutSlowInInterpolator()
    }

    @set:ModelProp
    lateinit var artist: Artist

    var lastArtist: Artist? = null

    @set:ModelProp(ModelProp.Option.DoNotHash)
    var listener: Listener? = null

    private var paletteTask: AsyncTask<Bitmap, Void, Palette>? = null

    private var paletteListener = Palette
            .PaletteAsyncListener {
                it?.mostPopulous()?.let {
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
        radius = 4f.dpToPx(context)

        setOnClickListener { listener?.onClick(artist) }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        paletteTask?.cancel(true)
    }

    @AfterPropsSet
    fun onChanged() {
        val changed = this.lastArtist?.id != artist.id
        if (changed) {
            setCardBackgroundColor(Color.TRANSPARENT)
            textName.setTextColor(Color.WHITE)
        }

        clearAnimation()

        this.lastArtist = artist

        textName.text = artist.name

        val spanCount = ArtistsUtils.spanCount(context)
        val targetSize = resources.displayMetrics.widthPixels / spanCount

        Picasso.get()
                .load(artist.image)
                .transform(ArtworkTransformation(targetSize))
                .into(image, paletteCallback)
    }

    private inner class PaletteResultAnimation(
            swatch: Palette.Swatch
    ) : Animation() {

        private val finalBackgroundColor = swatch.rgb
        private val finalTextColor = swatch.bodyTextColor.withMaxAlpha()

        init {
            duration = 300
            interpolator = INTERPOLATOR
        }

        override fun applyTransformation(
                interpolatedTime: Float,
                t: Transformation?
        ) {
            super.applyTransformation(interpolatedTime, t)

            setCardBackgroundColor(finalBackgroundColor.withAlpha(interpolatedTime))

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
        fun onClick(artist: Artist)
    }
}