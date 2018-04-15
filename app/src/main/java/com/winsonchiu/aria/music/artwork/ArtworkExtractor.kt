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
import com.winsonchiu.aria.folders.util.FileFilters
import com.winsonchiu.aria.folders.util.FileFilters.COVER_IMAGE_REGEX
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

    private val knownEmptyFolders = mutableSetOf<File>()

    fun getArtworkForFile(file: File, cache: ArtworkCache): Bitmap? {
        cache[file.absolutePath]?.let { return it.bitmap }

        return (searchEmbedded(file, cache)
                ?: searchMediaStore(file, cache)
                ?: searchFolderForCover(file, cache)
                ?: searchFolderForCover(file.parentFile, cache)
                ?: searchFolderForCover(file.parentFile?.parentFile, cache))
                .also {
                    if (it == null) {
                        cache[file.absolutePath] = ArtworkCache.EMPTY
                    }
                }
    }

    fun getArtworkWithFileDepthSearch(file: File, cache: ArtworkCache): Bitmap? {
        return getArtworkForFile(file, cache) ?: searchFolderForFirst(file, cache)
    }

    private fun <T> failsafeTry(block: () -> T) = try {
        block()
    } catch (e: Exception) {
        null
    }

    private fun searchEmbedded(file: File, cache: ArtworkCache) = failsafeTry {
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

    private fun searchMediaStore(file: File, cache: ArtworkCache) = failsafeTry {
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

    private fun searchFolderForCover(file: File?, cache: ArtworkCache) = failsafeTry {
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

    private fun searchFolderForFirst(file: File?, cache: ArtworkCache) = failsafeTry {
        if (file?.isDirectory != true) {
            return@failsafeTry null
        }

        val bitmap = file.walkTopDown()
                .maxDepth(3)
                .onEnter { !knownEmptyFolders.contains(it) }
                .onLeave { knownEmptyFolders.add(it) }
                .find { !it.isDirectory && getArtworkForFile(it, cache) != null }
                ?.let { cache[it.absolutePath]?.bitmap }
                ?: return@failsafeTry null

        cacheArtwork(file, cache, file.absolutePath) {
            bitmap
        }
    }

    private fun cacheArtwork(
            file: File,
            cache: ArtworkCache,
            key: String?,
            bitmapGenerator: () -> Bitmap
    ): Bitmap? {
        cache[key]?.bitmap?.let { return it }

        return bitmapGenerator()
                .also {
                    val metadata = ArtworkCache.Metadata(it)
                    cache[file.absolutePath] = metadata
                    cache[key] = metadata
                }
    }
}