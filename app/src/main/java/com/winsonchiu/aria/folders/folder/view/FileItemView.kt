package com.winsonchiu.aria.folders.folder.view

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.AfterPropsSet
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.winsonchiu.aria.R
import com.winsonchiu.aria.folders.folder.FolderController
import com.winsonchiu.aria.framework.util.DrawableUtils
import com.winsonchiu.aria.framework.util.initialize
import com.winsonchiu.aria.framework.util.textOrGone
import kotlinx.android.synthetic.main.file_item_view.view.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class FileItemView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    lateinit var fileMetadata: FolderController.FileMetadata
        @ModelProp set

    var title: String? = null
        @ModelProp set

    var description: String? = null
        @ModelProp set

    var listener: Listener? = null
        @ModelProp(ModelProp.Option.DoNotHash) set

    init {
        initialize(R.layout.file_item_view)

        background = DrawableUtils.getDefaultRipple(context, false)

        setOnClickListener { listener?.onClick(fileMetadata) }
        setOnLongClickListener { listener?.onLongClick(fileMetadata); true }
    }

    @AfterPropsSet
    fun onChanged() {
        fileNameText.text = title
        fileDescriptionText.textOrGone = description
        fileImage.setData(fileMetadata)
    }

    interface Listener {
        fun onClick(fileMetadata: FolderController.FileMetadata)
        fun onLongClick(fileMetadata: FolderController.FileMetadata)
    }
}