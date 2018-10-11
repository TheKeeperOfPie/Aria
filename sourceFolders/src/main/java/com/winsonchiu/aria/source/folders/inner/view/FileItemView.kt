package com.winsonchiu.aria.source.folders.inner.view

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.AfterPropsSet
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.winsonchiu.aria.framework.util.DrawableUtils
import com.winsonchiu.aria.framework.util.initialize
import com.winsonchiu.aria.framework.util.textOrGone
import com.winsonchiu.aria.source.folders.FileEntry
import com.winsonchiu.aria.source.folders.R
import kotlinx.android.synthetic.main.file_item_view.view.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class FileItemView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    lateinit var entry: FileEntry
        @ModelProp set

    var title: CharSequence? = null
        @ModelProp set

    var description: CharSequence? = null
        @ModelProp set

    var listener: Listener? = null
        @ModelProp(ModelProp.Option.DoNotHash) set

    init {
        initialize(R.layout.file_item_view)

        background = DrawableUtils.getDefaultRipple(context, false)

        setOnClickListener { listener?.onClick(entry) }
        setOnLongClickListener { listener?.onLongClick(entry); true }
    }

    @AfterPropsSet
    fun onChanged() {
        fileNameText.text = title
        fileDescriptionText.textOrGone = description
        fileImage.setData(entry)
    }

    interface Listener {
        fun onClick(entry: FileEntry)
        fun onLongClick(entry: FileEntry)
    }
}