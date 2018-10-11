package com.winsonchiu.aria.artwork

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import com.squareup.picasso.Picasso
import com.squareup.picasso.Request
import com.squareup.picasso.RequestHandler
import java.io.File
import javax.inject.Inject

@ArtworkScope
class ArtworkRequestHandler @Inject constructor(
        val application: Application,
        val artworkExtractor: ArtworkExtractor
) : RequestHandler() {

    companion object {

        private val ARTIST_ALBUMS_PROJECTION = arrayOf(
                MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Artists.Albums.ALBUM_ART
        )

        private val ARTIST_MEDIA_PROJECTION = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.DATA
        )

        private const val ARTIST_AUTHORITY = "${BuildConfig.APPLICATION_ID}.artist"

        private const val MUSIC_FILE_AUTHORITY = "${BuildConfig.APPLICATION_ID}.musicFile"

        private const val EMBEDDED_FILE_AUTHORITY = "${BuildConfig.APPLICATION_ID}.embedded"

        fun artistUri(artistId: String) = Uri.Builder()
                .scheme("image")
                .authority(ARTIST_AUTHORITY)
                .path(artistId)
                .build()

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
            ARTIST_AUTHORITY,
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
            ARTIST_AUTHORITY -> getArtist(uri)
            EMBEDDED_FILE_AUTHORITY -> getEmbedded(uri)
            MUSIC_FILE_AUTHORITY -> getMusicFile(uri)
            else -> Picasso.get().load(uri).get()
        }

        return bitmap?.let { Result(bitmap, Picasso.LoadedFrom.DISK) }
    }

    private fun getArtist(uri: Uri): Bitmap? {
        val artistId = uri.lastPathSegment ?: return null
        return tryArtistAlbums(artistId) ?: tryArtistMedia(artistId)
    }

    private fun tryArtistAlbums(artistId: String): Bitmap? {
        val idLong = artistId.toLongOrNull() ?: return null
        return application.contentResolver.query(
                MediaStore.Audio.Artists.Albums.getContentUri("external", idLong),
                ARTIST_ALBUMS_PROJECTION,
                null,
                null,
                null
        )?.use {
            if (it.moveToFirst()) {
                val albumArtIndex = it.getColumnIndex(MediaStore.Audio.Artists.Albums.ALBUM_ART)

                do {
                    val albumArt = it.getString(albumArtIndex)
                    if (!albumArt.isNullOrEmpty()) {
                        try {
                            return@use BitmapFactory.decodeFile(albumArt)
                        } catch (ignored: Exception) {
                        }
                    }
                } while (it.moveToNext())
            }

            null
        }
    }

    private fun tryArtistMedia(artistId: String): Bitmap? {
        return application.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                ARTIST_MEDIA_PROJECTION,
                "${MediaStore.Audio.Media.ARTIST_ID}=?",
                arrayOf(artistId),
                null
        )?.use {
            if (it.moveToFirst()) {
                val dataIndex = it.getColumnIndex(MediaStore.Audio.Media.DATA)

                do {
                    val filePath = it.getString(dataIndex)
                    if (!filePath.isNullOrEmpty()) {
                        try {
                            val file = File(filePath)
                            if (file.exists()) {
                                val imageUri = artworkExtractor.getArtwork(file)
                                if (imageUri != null) {
                                    val bitmapFromMedia = when (imageUri.authority) {
                                        EMBEDDED_FILE_AUTHORITY -> getEmbedded(imageUri)
                                        else -> Picasso.get().load(imageUri).get()
                                    }

                                    if (bitmapFromMedia != null) {
                                        return@use bitmapFromMedia
                                    }
                                }
                            }
                        } catch (ignored: Exception) {
                        }
                    }
                } while (it.moveToNext())
            }

            null
        }
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
        val imageUri = artworkExtractor.getArtwork(File(uri.path))

        return when (imageUri?.authority) {
            EMBEDDED_FILE_AUTHORITY -> getEmbedded(imageUri)
            else -> Picasso.get().load(imageUri).get()
        }
    }
}