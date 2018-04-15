package com.winsonchiu.aria.music.artwork

import android.app.Application
import android.content.ContentUris
import android.database.DatabaseUtils
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import com.squareup.picasso.Picasso
import com.winsonchiu.aria.dagger.ApplicationScope
import com.winsonchiu.aria.folder.util.FileFilters
import com.winsonchiu.aria.folder.util.FileFilters.COVER_IMAGE_REGEX
import java.io.File
import java.util.Arrays
import javax.inject.Inject

@ApplicationScope
class ArtworkExtractor @Inject constructor(
        private val application: Application
) {
    private val mediaMetadataRetriever = object : ThreadLocal<MediaMetadataRetriever>() {
        override fun initialValue(): MediaMetadataRetriever {
            return MediaMetadataRetriever()
        }
    }

    fun getArtwork(file: File, cache: MutableMap<String?, Bitmap?>): Bitmap? {
        return searchCache(file, cache)
                ?: searchEmbedded(file, cache)
                ?: searchMediaStore(file, cache)
                ?: searchFolder(file, cache)
                ?: searchFolder(file.parentFile, cache)
                ?: searchFolder(file.parentFile?.parentFile, cache)
    }

    private fun <T> failsafeTry(block: () -> T) = try {
        block()
    } catch (e: Exception) {
        null
    }

    private fun searchCache(file: File, cache: MutableMap<String?, Bitmap?>): Bitmap? {
        return cache[file.absolutePath]
    }

    private fun searchEmbedded(file: File, cache: MutableMap<String?, Bitmap?>) = failsafeTry {
        if (file.isDirectory) {
            return@failsafeTry null
        }

        mediaMetadataRetriever.get().run {
            setDataSource(file.absolutePath)
            embeddedPicture?.let {
                cacheArtwork(file, cache, Arrays.hashCode(it).toString()) {
                    BitmapFactory.decodeByteArray(it, 0, it.size)
                }
            }
        }
    }

    private fun searchMediaStore(file: File, cache: MutableMap<String?, Bitmap?>) = failsafeTry {
        if (file.isDirectory) {
            return@failsafeTry null
        }

        val mediaStoreUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val columns = arrayOf(MediaStore.Audio.Media.ALBUM_ID)
        val where = "${MediaStore.Audio.Media.IS_MUSIC} = ? AND ${MediaStore.Audio.Media.DATA} = ?"
        val selectionArgs = arrayOf("1", DatabaseUtils.sqlEscapeString(file.absolutePath))

        val uri = application.contentResolver.query(mediaStoreUri, columns, where, selectionArgs, null)?.use {
            if (it.count == 0) {
                return@use null
            }

            it.moveToFirst()
            val albumId = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
            val artworkUri = Uri.parse("content://media/external/audio/albumart")
            ContentUris.withAppendedId(artworkUri, albumId)
        }

        if (uri == null) {
            null
        } else {
            cacheArtwork(file, cache, uri.toString()) {
                Picasso.get().load(uri).get()
            }
        }
    }

    private fun cacheArtwork(
            file: File,
            cache: MutableMap<String?, Bitmap?>,
            key: String?,
            bitmapGenerator: () -> Bitmap
    ): Bitmap? {
        cache[key]?.let { return it }
        return bitmapGenerator()
                .also {
                    cache[file.absolutePath] = it
                    cache[key] = it
                }
    }

    private fun searchFolder(file: File?, cache: MutableMap<String?, Bitmap?>) = failsafeTry {
        if (file?.isDirectory != true) {
            return@failsafeTry null
        }

        val coverImage = file.listFiles(FileFilters.IMAGES)
                .asSequence()
                .find {
                    it.name.matches(COVER_IMAGE_REGEX)
                }

        if (coverImage == null) {
            null
        } else {
            cacheArtwork(file, cache, coverImage.absolutePath) {
                Picasso.get().load(Uri.fromFile(coverImage)).get()
            }
        }
    }
}