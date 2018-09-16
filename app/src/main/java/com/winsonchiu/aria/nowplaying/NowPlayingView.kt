package com.winsonchiu.aria.nowplaying

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.util.TypedValue
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.doOnNextLayout
import com.squareup.picasso.Picasso
import com.winsonchiu.aria.R
import com.winsonchiu.aria.framework.util.initialize
import com.winsonchiu.aria.music.artwork.ArtworkTransformation
import kotlinx.android.synthetic.main.now_playing_view_constraint_expanded_as_merge.view.*

class NowPlayingView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : MotionLayout(context, attrs, defStyleAttr) {

    private val artworkTransformation = ArtworkTransformation()

    init {
        initialize(R.layout.now_playing_view_constraint_expanded_as_merge)
        loadLayoutDescription(R.xml.now_playing_view_scene)
        isClickable = true

        TypedValue().apply {
            context.theme.resolveAttribute(android.R.attr.windowBackground, this, true)
            setBackgroundColor(data)
        }

        doOnNextLayout {
            doOnNextLayout { progress = 0f }
            requestLayout()
        }
    }

    override fun setProgress(pos: Float) {
        super.setProgress(pos)
        textSongTitle.progress = pos
    }

    fun bindData(data: Model) {
        Picasso.get()
                .load(data.image)
                .transform(artworkTransformation)
                .into(imageArtwork)

        textSongTitle.text = data.title
        textSongDescription.text = data.description
    }

    data class Model(
            val title: CharSequence?,
            val description: CharSequence?,
            val image: Uri?
    )
}