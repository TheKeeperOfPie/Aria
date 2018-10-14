package com.winsonchiu.aria.artwork

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import androidx.collection.LruCache
import com.squareup.picasso.Picasso
import com.squareup.picasso.Request
import com.squareup.picasso.RequestHandler
import com.winsonchiu.aria.artwork.lastfm.LastFmApi
import java.io.File
import javax.inject.Inject

@ArtworkScope
class ArtworkRequestHandler @Inject constructor(
        private val application: Application,
        private val artworkExtractor: ArtworkExtractor,
        private val lastFmApi: LastFmApi
) : RequestHandler() {

    companion object {

        private const val BITMAP_CACHE_SIZE = 500 * 1024 * 1024

        private val ARTIST_NAME_PROJECTION = arrayOf(
                MediaStore.Audio.Artists._ID,
                MediaStore.Audio.Artists.ARTIST
        )

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

    private val memoryCache: LruCache<String, Bitmap> = object : LruCache<String, Bitmap>(BITMAP_CACHE_SIZE) {
        override fun sizeOf(
                key: String,
                value: Bitmap
        ): Int {
            return value.byteCount
        }

        override fun entryRemoved(
                evicted: Boolean,
                key: String,
                oldValue: Bitmap,
                newValue: Bitmap?
        ) {
            super.entryRemoved(evicted, key, oldValue, newValue)
            oldValue.recycle()
        }
    }

    private val mediaMetadataRetriever: ThreadLocal<MediaMetadataRetriever> = object : ThreadLocal<MediaMetadataRetriever>() {
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

        val cached = memoryCache[uri.toString()]
        if (cached != null) {
            try {
                return Result(cached.copy(cached.config, true), Picasso.LoadedFrom.MEMORY)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val bitmap = when (uri?.authority) {
            ARTIST_AUTHORITY -> getArtist(uri)
            EMBEDDED_FILE_AUTHORITY -> getEmbedded(uri)
            MUSIC_FILE_AUTHORITY -> getMusicFile(uri)
            else -> Picasso.get().load(uri).get()
        }

        try {
            bitmap?.let {
                memoryCache.put(uri.toString(), bitmap.copy(bitmap.config, true))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return bitmap?.let { Result(bitmap, Picasso.LoadedFrom.DISK) }
    }

    private fun getArtist(uri: Uri): Bitmap? {
        val artistId = uri.lastPathSegment ?: return null
        return tryArtistLastFm(artistId) ?: tryArtistAlbums(artistId) ?: tryArtistMedia(artistId)
    }

    private fun tryArtistLastFm(artistId: String): Bitmap? = runCatching {
        return application.contentResolver.query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                ARTIST_NAME_PROJECTION,
                "${MediaStore.Audio.Artists._ID}=?",
                arrayOf(artistId),
                null
        )?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(MediaStore.Audio.Artists.ARTIST)

                val locales = application.resources.configuration.locales
                val lang = if (locales.isEmpty) "" else locales[0].language

                do {
                    val name = it.getString(nameIndex)
                    if (!name.isNullOrEmpty()) {
                        try {
                            val response = lastFmApi.artist(name, lang).execute()

                            if (response.isSuccessful) {
                                // TODO: Image fallbacks?
                                val imageUrl = response.body()
                                        ?.artist
                                        ?.images
                                        ?.lastOrNull()
                                        ?.url

                                if (!imageUrl.isNullOrEmpty()) {
                                    return@use Picasso.get().load(imageUrl).get()
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } while (it.moveToNext())
            }

            null
        }
    }.getOrNull()

    private fun tryArtistAlbums(artistId: String): Bitmap? = runCatching {
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
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } while (it.moveToNext())
            }

            null
        }
    }.getOrNull()

    private fun tryArtistMedia(artistId: String): Bitmap? = runCatching {
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
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } while (it.moveToNext())
            }

            null
        }
    }.getOrNull()

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