package com.winsonchiu.aria.queue.ui.view

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
import butterknife.OnClick
import com.airbnb.epoxy.AfterPropsSet
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.squareup.picasso.Picasso
import com.winsonchiu.aria.artwork.ArtworkTransformation
import com.winsonchiu.aria.framework.util.DrawableUtils
import com.winsonchiu.aria.framework.util.dpToPx
import com.winsonchiu.aria.framework.util.initialize
import com.winsonchiu.aria.queue.QueueEntry
import com.winsonchiu.aria.queue.R
import kotlinx.android.synthetic.main.queue_item_view.view.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class QueueItemView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    @set:ModelProp
    lateinit var queueEntry: QueueEntry

    @set:ModelProp
    var title: CharSequence? = null

    @set:ModelProp
    var showSelected: Boolean = false

    @set:ModelProp(ModelProp.Option.DoNotHash)
    var listener: Listener? = null

    val colorHighlight by lazy {
        TypedValue().let {
            context.theme.resolveAttribute(R.attr.colorControlHighlight, it, true)
            it.data
        }
    }

    private val artworkTransformation = ArtworkTransformation(56.dpToPx(context))

    init {
        initialize(R.layout.queue_item_view)

        background = DrawableUtils.getDefaultRipple(context, false)

        viewDragHandle.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                listener?.onStartDrag(this)
            }

            false
        }
    }

    @AfterPropsSet
    fun onChanged() {
        Picasso.get()
                .load(queueEntry.image)
                .transform(artworkTransformation)
                .into(itemImage)

        itemTitleText.text = title

        foreground = if (showSelected) {
            ColorDrawable(colorHighlight)
        } else {
            null
        }
    }

    @OnClick()
    fun onClick() {
        listener?.onClick(queueEntry)
    }

    interface Listener {
        fun onClick(queueEntry: QueueEntry)
        fun onStartDrag(view: QueueItemView)
    }
}