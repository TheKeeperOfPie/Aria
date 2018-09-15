package com.winsonchiu.aria.folders.folder

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.AfterPropsSet
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.winsonchiu.aria.R
import com.winsonchiu.aria.framework.util.initialize
import kotlinx.android.synthetic.main.file_section_header_view.view.fileSectionHeaderText

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class FileSectionHeaderView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    lateinit var text: String
        @ModelProp set

    init {
        initialize(R.layout.file_section_header_view)
    }

    @AfterPropsSet
    fun onChanged() {
        fileSectionHeaderText.text = text
    }
}