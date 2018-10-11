package com.winsonchiu.aria.source.artists

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.AfterPropsSet
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.squareup.picasso.Picasso
import com.winsonchiu.aria.artwork.ArtworkTransformation
import com.winsonchiu.aria.framework.util.initialize
import kotlinx.android.synthetic.main.artist_item_view.view.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ArtistItemView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    @set:ModelProp
    lateinit var artist: Artist

    @set:ModelProp(ModelProp.Option.DoNotHash)
    var listener: Listener? = null

    init {
        initialize(R.layout.artist_item_view)

        setOnClickListener { listener?.onClick(artist) }
    }

    @AfterPropsSet
    fun onChanged() {
        textName.text = artist.name
        textTrackCount.text = "${artist.trackCount}"

        val spanCount = ArtistsUtils.spanCount(context)
        val targetSize = resources.displayMetrics.widthPixels / spanCount

        Picasso.get()
                .load(artist.image)
                .transform(ArtworkTransformation(targetSize))
                .into(image)
    }

    interface Listener {
        fun onClick(artist: Artist)
    }
}