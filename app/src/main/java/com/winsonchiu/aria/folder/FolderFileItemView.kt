package com.winsonchiu.aria.folder

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import butterknife.OnClick
import com.airbnb.epoxy.AfterPropsSet
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.winsonchiu.aria.R
import com.winsonchiu.aria.util.initialize
import kotlinx.android.synthetic.main.folder_file_item_view.view.fileNameText
import java.io.File

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class FolderFileItemView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    lateinit var file: File
        @ModelProp set

    var listener: Listener? = null
        @ModelProp(ModelProp.Option.DoNotHash) set

    init {
        initialize(R.layout.folder_file_item_view)
    }

    @AfterPropsSet
    fun onChanged() {
        fileNameText.text = file.nameWithoutExtension
    }

    @OnClick()
    fun onClick() {
        listener?.onClick(file)
    }

    interface Listener {
        fun onClick(file: File)
    }
}