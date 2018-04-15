package com.winsonchiu.aria.folder

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import butterknife.OnClick
import com.airbnb.epoxy.AfterPropsSet
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.winsonchiu.aria.R
import com.winsonchiu.aria.util.DrawableUtils
import com.winsonchiu.aria.util.initialize
import com.winsonchiu.aria.util.textOrGone
import kotlinx.android.synthetic.main.folder_file_item_view.view.fileDescriptionText
import kotlinx.android.synthetic.main.folder_file_item_view.view.fileImage
import kotlinx.android.synthetic.main.folder_file_item_view.view.fileNameText

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class FolderFileItemView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    lateinit var fileModel: FolderController.FileModel
        @ModelProp set

    var listener: Listener? = null
        @ModelProp(ModelProp.Option.DoNotHash) set

    init {
        initialize(R.layout.folder_file_item_view)

        background = DrawableUtils.getDefaultRipple(context, false)
    }

    @AfterPropsSet
    fun onChanged() {
        val (file, image) = fileModel
        fileNameText.text = file.name
        fileDescriptionText.textOrGone("")
        fileImage.setImageBitmap(image)
    }

    @OnClick()
    fun onClick() {
        listener?.onClick(fileModel)
    }

    interface Listener {
        fun onClick(fileModel: FolderController.FileModel)
    }
}