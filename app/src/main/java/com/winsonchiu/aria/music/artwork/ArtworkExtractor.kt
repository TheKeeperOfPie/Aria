package com.winsonchiu.aria.music.artwork

import android.app.Application
import android.content.ContentUris
import android.database.DatabaseUtils
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.support.annotation.WorkerThread
import com.squareup.picasso.Picasso
import com.winsonchiu.aria.dagger.HomeFragmentScreenScope
import com.winsonchiu.aria.folders.util.FileFilters
import com.winsonchiu.aria.folders.util.FileFilters.COVER_IMAGE_REGEX
import com.winsonchiu.aria.util.Failsafe
import com.winsonchiu.aria.util.dpToPx
import java.io.File
import java.util.Arrays
import javax.inject.Inject

@HomeFragmentScreenScope
class ArtworkExtractor @Inject constructor(
        private val application: Application
) {
    private val mediaMetadataRetriever = object : ThreadLocal<MediaMetadataRetriever>() {
        override fun initialValue(): MediaMetadataRetriever {
            return MediaMetadataRetriever()
        }
    }

    private val knownEmptyFolders = mutableSetOf<File>()

    private val targetSize by lazy { 60.dpToPx(application) }

    @WorkerThread
    fun getArtworkForFile(file: File, cache: ArtworkCache): ArtworkCache.Metadata? {
        cache[file.absolutePath]?.let { return it }

        val metadata = (searchEmbedded(file, cache)
                ?: searchMediaStore(file, cache)
                ?: searchFolderForCover(file, file, cache)
                ?: searchFolderForCover(file, file.parentFile, cache)
                ?: searchFolderForCover(file, file.parentFile?.parentFile, cache))

        if (metadata == null && !file.isDirectory) {
            // If we're found nothing and this isn't a directory, we know for sure there's no image
            return cache.getOrPut(file.absolutePath) { ArtworkCache.EMPTY }
        }

        return metadata
    }

    @WorkerThread
    fun getArtworkWithFileDepthSearch(file: File, cache: ArtworkCache): ArtworkCache.Metadata? {
        return getArtworkForFile(file, cache)
                ?: searchFolderForFirst(file, cache)
                ?: cache.getOrPut(file.absolutePath) { ArtworkCache.EMPTY }
    }

    private fun searchEmbedded(file: File, cache: ArtworkCache) = Failsafe.orNull {
        mediaMetadataRetriever.get().run {
            setDataSource(file.absolutePath)
            embeddedPicture?.let {
                cacheArtwork(file, cache, Arrays.hashCode(it).toString()) {
                    BitmapFactory.decodeByteArray(it, 0, it.size)
                }
            }
        }
    }

    private fun searchMediaStore(file: File, cache: ArtworkCache) = Failsafe.orNull {
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
        } ?: return@orNull null

        cacheArtwork(file, cache, uri.toString()) {
            Picasso.get()
                    .load(uri)
                    .centerCrop()
                    .resize(targetSize, targetSize)
                    .onlyScaleDown()
                    .get()
        }
    }

    private fun searchFolderForCover(sourceFile: File, folderToSearch: File?, cache: ArtworkCache) = Failsafe.orNull {
        val coverImage = folderToSearch?.listFiles(FileFilters.IMAGES)
                ?.find {
                    it.name.matches(COVER_IMAGE_REGEX)
                } ?: return@orNull null

        cacheArtwork(sourceFile, cache, coverImage.absolutePath) {
            Picasso.get()
                    .load(Uri.fromFile(coverImage))
                    .centerCrop()
                    .resize(targetSize, targetSize)
                    .onlyScaleDown()
                    .get()
        }
    }

    private fun searchFolderForFirst(file: File?, cache: ArtworkCache) = Failsafe.orNull {
        val bitmap = file?.walkTopDown()
                ?.maxDepth(3)
                ?.onEnter { !knownEmptyFolders.contains(it) }
                ?.onLeave { knownEmptyFolders.add(it) }
                ?.find { !it.isDirectory && getArtworkForFile(it, cache)?.bitmap != null }
                ?.let { cache[it.absolutePath]?.bitmap }
                ?: return@orNull null

        cacheArtwork(file, cache, file.absolutePath) {
            bitmap
        }
    }

    private fun cacheArtwork(
            file: File,
            cache: ArtworkCache,
            key: String?,
            bitmapGenerator: () -> Bitmap
    ): ArtworkCache.Metadata? {
        val cached = cache[key]
        if (cached?.bitmap != null) {
            return cached
        }

        return bitmapGenerator()
                .let(::scaleIfNecessary)
                .let {
                    val metadata = ArtworkCache.Metadata(it)
                    cache[file.absolutePath] = metadata
                    cache[key] = metadata
                    metadata
                }
    }

    private fun scaleIfNecessary(bitmap: Bitmap): Bitmap {
        return if (bitmap.width != targetSize || bitmap.height != targetSize) {
            ThumbnailUtils.extractThumbnail(bitmap, targetSize, targetSize, ThumbnailUtils.OPTIONS_RECYCLE_INPUT)
        } else {
            bitmap
        }
    }
}