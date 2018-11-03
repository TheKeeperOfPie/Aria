package com.winsonchiu.aria.source.artists.artist.media

import android.content.Context
import android.text.format.DateUtils
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.AfterPropsSet
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.winsonchiu.aria.framework.util.DrawableUtils
import com.winsonchiu.aria.framework.util.initialize
import com.winsonchiu.aria.framework.util.textOrGone
import com.winsonchiu.aria.source.artists.R
import kotlinx.android.synthetic.main.artist_media_item_view.view.*
import java.util.concurrent.TimeUnit

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ArtistMediaItemView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    @set:ModelProp
    lateinit var data: ArtistMedia

    @set:CallbackProp
    var listener: Listener? = null

    init {
        initialize(R.layout.artist_media_item_view)

        background = DrawableUtils.getDefaultRipple(context, false)

        setOnClickListener { listener?.onClick(data) }
        setOnLongClickListener { listener?.onLongClick(data); true }
    }

    @AfterPropsSet
    fun onChanged() {
        textTitle.text = data.title
        textDescription.textOrGone = data.description
        textDuration.textOrGone = data.duration?.let { DateUtils.formatElapsedTime(TimeUnit.MILLISECONDS.toSeconds(it)) }
    }

    interface Listener {
        fun onClick(data: ArtistMedia)
        fun onLongClick(data: ArtistMedia)
    }
}