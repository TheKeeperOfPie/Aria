package com.winsonchiu.aria.queue.view

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
import com.winsonchiu.aria.R
import com.winsonchiu.aria.framework.util.DrawableUtils
import com.winsonchiu.aria.framework.util.initialize
import com.winsonchiu.aria.media.MediaQueue
import kotlinx.android.synthetic.main.queue_item_view.view.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class QueueItemView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    @set:ModelProp
    lateinit var queueItem: MediaQueue.QueueItem

    @set:ModelProp
    var title: String? = null

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
        fileImage.setImageBitmap(queueItem.image?.bitmap)
        fileNameText.text = title

        foreground = if (showSelected) {
            ColorDrawable(colorHighlight)
        } else {
            null
        }
    }

    @OnClick()
    fun onClick() {
        listener?.onClick(queueItem)
    }

    interface Listener {
        fun onClick(queueItem: MediaQueue.QueueItem)
        fun onStartDrag(view: QueueItemView)
    }
}