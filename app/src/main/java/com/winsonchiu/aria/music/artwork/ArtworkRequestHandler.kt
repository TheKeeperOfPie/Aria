package com.winsonchiu.aria.music.artwork

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import com.squareup.picasso.Picasso
import com.squareup.picasso.Request
import com.squareup.picasso.RequestHandler
import com.winsonchiu.aria.BuildConfig
import com.winsonchiu.aria.framework.dagger.ApplicationScope
import java.io.File
import java.net.URLDecoder
import javax.inject.Inject

@ApplicationScope
class ArtworkRequestHandler @Inject constructor(
        val artworkExtractor: ArtworkExtractor
) : RequestHandler() {

    companion object {

        val MUSIC_FILE_AUTHORITY = "${BuildConfig.APPLICATION_ID}.musicFile"

        val EMBEDDED_FILE_AUTHORITY = "${BuildConfig.APPLICATION_ID}.embedded"

        fun musicFileUri(file: File) = Uri.Builder()
                .scheme("image")
                .authority(MUSIC_FILE_AUTHORITY)
                .path(file.absolutePath)
                .build()

        fun embeddedUri(file: File) = Uri.Builder()
                .scheme("image")
                .authority(EMBEDDED_FILE_AUTHORITY)
                .path(file.absolutePath)
                .build()
    }

    private val mediaMetadataRetriever = object : ThreadLocal<MediaMetadataRetriever>() {
        override fun initialValue(): MediaMetadataRetriever {
            return MediaMetadataRetriever()
        }
    }

    override fun canHandleRequest(data: Request?): Boolean {
        return when (data?.uri?.authority) {
            EMBEDDED_FILE_AUTHORITY,
            MUSIC_FILE_AUTHORITY -> true
            else -> false
        }
    }

    override fun load(
            request: Request?,
            networkPolicy: Int
    ): Result? {
        val uri = request?.uri
        val bitmap = when (uri?.authority) {
            EMBEDDED_FILE_AUTHORITY -> getEmbedded(uri)
            MUSIC_FILE_AUTHORITY -> getMusicFile(uri)
            else -> Picasso.get().load(uri).get()
        }

        return bitmap?.let { Result(bitmap, Picasso.LoadedFrom.DISK) }
    }

    private fun getEmbedded(uri: Uri): Bitmap? {
        return mediaMetadataRetriever.get()!!.run {
            setDataSource(File(uri.path).absolutePath)
            embeddedPicture?.let {
                BitmapFactory.decodeByteArray(it, 0, it.size)
            }
        }
    }

    private fun getMusicFile(uri: Uri): Bitmap? {
        val filePath = URLDecoder.decode(uri.path, "UTF-8")
        val imageUri = artworkExtractor.getArtwork(File(uri.path))

        Log.d("ArtworkRequestHandler", "load music file for $filePath, $imageUri")

        return when (imageUri?.authority) {
            EMBEDDED_FILE_AUTHORITY -> getEmbedded(imageUri)
            else -> Picasso.get().load(imageUri).get()
        }
    }
}