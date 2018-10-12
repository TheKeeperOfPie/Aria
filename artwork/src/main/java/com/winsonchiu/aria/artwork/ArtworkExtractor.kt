package com.winsonchiu.aria.artwork

import android.app.Application
import android.content.ContentUris
import android.database.DatabaseUtils
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import androidx.annotation.WorkerThread
import androidx.collection.LruCache
import com.winsonchiu.aria.framework.util.Failsafe
import com.winsonchiu.aria.framework.util.FileFilters
import com.winsonchiu.aria.framework.util.FileFilters.COVER_IMAGE_REGEX
import com.winsonchiu.aria.framework.util.set
import java.io.File
import java.util.Collections
import javax.inject.Inject

@ArtworkScope
class ArtworkExtractor @Inject constructor(
        private val application: Application
) {

    companion object {
        private val NONE_CACHE_SIZE = 1000
    }

    // Set with a max size, evicting oldest entry
    private val knownNoneCache = Collections.newSetFromMap(object : LinkedHashMap<String, Boolean>() {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Boolean>?): Boolean {
            return size > NONE_CACHE_SIZE
        }
    })

    private val mediaMetadataRetriever: ThreadLocal<MediaMetadataRetriever> = object : ThreadLocal<MediaMetadataRetriever>() {
        override fun initialValue(): MediaMetadataRetriever {
            return MediaMetadataRetriever()
        }
    }

    private val knownEmptyFolders = mutableSetOf<File>()

    private val embeddedCache = LruCache<String, Uri>(1000)
    private val coverCache = LruCache<String, Uri>(1000)

    @WorkerThread
    fun getArtwork(file: File): Uri? {
        val key = file.absolutePath
        val fromCache = embeddedCache[key] ?: coverCache[key]
        if (fromCache != null) {
            return fromCache
        }

        return if (file.isDirectory) {
            getArtworkForDirectory(file)
        } else {
            getArtworkForFile(file)
        }
    }

    private fun getArtworkForFile(file: File): Uri? {
        val key = file.absolutePath

        val uri = searchEmbedded(file)
                ?: searchMediaStore(file)
                ?: searchFolderForCover(file)
                ?: searchFolderForCover(file.parentFile)
                ?: searchFolderForCover(file.parentFile?.parentFile)
                ?: file.parentFile?.let {
                    ArtworkFileTreeWalk(it)
                            .maxDepth(2)
                            .filter { it.isDirectory }
                            .mapNotNull { searchFolderForCover(it) }
                            .firstOrNull()
                }

        if (uri == null) {
            knownNoneCache += key
        } else {
            coverCache[key] = uri
        }

        return uri
    }


    private fun getArtworkForDirectory(file: File): Uri? {
        val files = file.listFiles(FileFilters.AUDIO)

        val embedded = files.asSequence()
                .mapNotNull(::searchEmbedded)
                .firstOrNull()

        if (embedded != null) {
            return embedded
        }

        val cover = searchFolderForCover(file)
        if (cover != null) {
            return cover
        }

        val search = searchFolderForFirst(file)
        if (search != null) {
            return search
        }

        knownNoneCache += file.absolutePath

        return null
    }

    // TODO: Replace failsafe with a debug-only error icon
    private fun searchEmbedded(file: File) = Failsafe.orNull {
        val embedded = mediaMetadataRetriever.get()!!.run {
            setDataSource(file.absolutePath)
            embeddedPicture?.let { ArtworkRequestHandler.embeddedUri(file) }
        }

        if (embedded != null) {
            embeddedCache[file.absolutePath] = embedded
        }

        embedded
    }

    private fun searchMediaStore(file: File) = Failsafe.orNull {
        val mediaStoreUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val columns = arrayOf(MediaStore.Audio.Media.ALBUM_ID)
        val where = "${MediaStore.Audio.Media.IS_MUSIC} = ? AND ${MediaStore.Audio.Media.DATA} = ?"
        val selectionArgs = arrayOf("1", DatabaseUtils.sqlEscapeString(file.absolutePath))

        application.contentResolver.query(mediaStoreUri, columns, where, selectionArgs, null)?.use {
            if (it.count == 0) {
                return@use null
            }

            it.moveToFirst()
            val albumId = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
            val artworkUri = Uri.parse("content://media/external/audio/albumart")
            ContentUris.withAppendedId(artworkUri, albumId)
        }
    }

    private fun searchFolderForCover(
            folderToSearch: File?
    ) = Failsafe.orNull {
        val files = folderToSearch?.listFiles(FileFilters.IMAGES)
        var cover = files
                ?.asSequence()
                ?.find { it.name.matches(COVER_IMAGE_REGEX) }

        if (cover == null) {
            cover = files
                    ?.map {
                        it to it.name.asSequence()
                                .filter { it.isDigit() }
                                .joinToString() + it.name
                    }
                    ?.sortedBy { it.second }
                    ?.map { it.first }
                    ?.firstOrNull()
        }

        if (cover == null) {
            cover = files?.firstOrNull()
        }

        cover?.let(Uri::fromFile)
    }

    private fun searchFolderForFirst(
            file: File
    ) = Failsafe.orNull {
        ArtworkFileTreeWalk(file)
                .maxDepth(3)
                .onEnter { !knownEmptyFolders.contains(it) }
                .onLeave { knownEmptyFolders.add(it) }
                .filter { it.isFile }
                .mapNotNull { getArtworkForFile(it) }
                .firstOrNull()
    }
}