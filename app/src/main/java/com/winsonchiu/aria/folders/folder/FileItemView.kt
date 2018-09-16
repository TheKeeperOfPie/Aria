package com.winsonchiu.aria.folders.folder

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import butterknife.BindDrawable
import com.airbnb.epoxy.AfterPropsSet
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.winsonchiu.aria.R
import com.winsonchiu.aria.framework.util.DrawableUtils
import com.winsonchiu.aria.framework.util.dpToPx
import com.winsonchiu.aria.framework.util.initialize
import com.winsonchiu.aria.framework.util.textOrGone
import com.winsonchiu.aria.music.artwork.ArtworkTransformation
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

    @BindDrawable(R.drawable.folder_file_image_directory)
    lateinit var overlayImageDirectory: Drawable

    @BindDrawable(R.drawable.folder_file_image_music)
    lateinit var overlayImageMusic: Drawable

    @BindDrawable(R.color.folderOverlayGray)
    lateinit var overlayColor: Drawable

    private val artworkTransformation = ArtworkTransformation(70.dpToPx(context))

    private val errorCallback = object : Callback.EmptyCallback() {
        override fun onError(e: Exception?) {
            fileImage.foreground = overlayImageMusic
        }
    }

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

        val image = fileMetadata.image

        fileImage.foreground = when {
            fileMetadata.file.isDirectory -> overlayImageDirectory
            image == null -> overlayImageMusic
            else -> overlayColor
        }

        Picasso.get()
                .load(image)
                .transform(artworkTransformation)
                .into(fileImage, errorCallback)
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Picasso.get().cancelRequest(fileImage)
    }

    interface Listener {
        fun onClick(fileMetadata: FolderController.FileMetadata)
        fun onLongClick(fileMetadata: FolderController.FileMetadata)
    }
}