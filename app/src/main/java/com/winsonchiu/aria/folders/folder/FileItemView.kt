package com.winsonchiu.aria.folders.folder

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import butterknife.BindDrawable
import butterknife.OnClick
import com.airbnb.epoxy.AfterPropsSet
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.winsonchiu.aria.R
import com.winsonchiu.aria.music.artwork.ArtworkCache
import com.winsonchiu.aria.util.DrawableUtils
import com.winsonchiu.aria.util.initialize
import com.winsonchiu.aria.util.textOrGone
import kotlinx.android.synthetic.main.file_item_view.view.fileDescriptionText
import kotlinx.android.synthetic.main.file_item_view.view.fileImage
import kotlinx.android.synthetic.main.file_item_view.view.fileImageOverlay
import kotlinx.android.synthetic.main.file_item_view.view.fileNameText
import java.io.File

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class FileItemView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    lateinit var file: File
        @ModelProp set

    var image: ArtworkCache.Metadata? = null
        @ModelProp set

    var description: String? = null
        @ModelProp set

    var listener: Listener? = null
        @ModelProp(ModelProp.Option.DoNotHash) set

    @BindDrawable(R.drawable.folder_file_image_directory)
    lateinit var fileImageForeground: Drawable

    @BindDrawable(R.drawable.folder_file_image_music)
    lateinit var imageMusic: Drawable

    init {
        initialize(R.layout.file_item_view)

        background = DrawableUtils.getDefaultRipple(context, false)
    }

    @AfterPropsSet
    fun onChanged() {
        fileNameText.text = file.name

        fileDescriptionText.textOrGone(description)

        val image = image

        fileImage.setImageBitmap(image?.bitmap)

        fileImageOverlay.background = when {
            file.isDirectory -> fileImageForeground
            image != null && image.bitmap == null -> imageMusic
            else -> null
        }
    }

    @OnClick()
    fun onClick() {
        listener?.onClick(file)
    }

    interface Listener {
        fun onClick(file: File)
    }
}