package com.winsonchiu.aria.source.folders.inner.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.withSave
import butterknife.ButterKnife
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.winsonchiu.aria.artwork.ArtworkTransformation
import com.winsonchiu.aria.framework.util.dpToPx
import com.winsonchiu.aria.source.folders.FileEntry
import com.winsonchiu.aria.source.folders.R

class FileImageView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val radius = 8f.dpToPx(this)

    private val path = Path()

    private val folderCutoutPath = Path()
    private val folderPath = Path()
    private val folderPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f.dpToPx(context)
        color = Color.WHITE
        isAntiAlias = true
    }

    private var data: FileEntry? = null

    val overlayImageMusic by lazy {
        AppCompatResources.getDrawable(context, R.drawable.folder_file_image_music)
    }

    val overlayImagePlaylist by lazy {
        AppCompatResources.getDrawable(context, R.drawable.folder_file_image_playlist)
    }

    private val artworkTransformation = ArtworkTransformation(70.dpToPx(context))

    private val errorCallback = object : Callback.EmptyCallback() {
        override fun onError(e: Exception?) {
            foreground = when {
                data?.file?.isDirectory == true -> null
                else -> overlayImageMusic
            }
        }
    }

    init {
        ButterKnife.bind(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Picasso.get().cancelRequest(this)
    }

    override fun onSizeChanged(
            width: Int,
            height: Int,
            oldWidth: Int,
            oldHeight: Int
    ) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        if (width != oldWidth || height != oldHeight) {
            path.reset()
            path.addRoundRect(0f, 0f, width.toFloat(), height.toFloat(), radius, radius, Path.Direction.CW)

            folderCutoutPath.apply {
                reset()
                moveTo(0.4f * width, 0f)
                rLineTo(radius, radius)
                lineTo(width - radius, radius)
                rQuadTo(radius, 0f, radius, radius)
                lineTo(width.toFloat(), 0f)
                close()
            }

            folderPath.op(path, folderCutoutPath, Path.Op.DIFFERENCE)
        }
    }

    fun setData(data: FileEntry) {
        this.data = data

        val image = data.image

        foreground = when (image) {
            null -> when (data) {
                is FileEntry.Folder -> null
                is FileEntry.Playlist -> overlayImagePlaylist
                is FileEntry.Audio -> overlayImageMusic
            }
            else -> null
        }

        Picasso.get()
                .load(image)
                .transform(artworkTransformation)
                .into(this, errorCallback)
    }

    override fun draw(canvas: Canvas) {
        canvas.withSave {
            if (data?.file?.isDirectory == true) {
                canvas.clipPath(folderPath)
                super.draw(canvas)
                canvas.drawPath(folderPath, folderPaint)
            } else {
                canvas.clipPath(path)
                super.draw(canvas)
            }
        }
    }
}