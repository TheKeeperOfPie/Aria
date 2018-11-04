package com.winsonchiu.aria.framework.media

import android.content.Context
import com.winsonchiu.aria.framework.text.TextConverter

internal fun String?.isShowable() = !isNullOrBlank()
        && !equals("unknown", ignoreCase = true)
        && !equals("null", ignoreCase = true)

object AudioMetadataUtils {

    fun getDescription(context: Context, artist: String?, album: String?): CharSequence? {
        val resources = context.resources

        val artistShowable = artist.isShowable()
        val albumShowable = album.isShowable()

        return when {
            artistShowable && albumShowable -> resources.getString(
                    R.string.audioMetadataFormatArtistAndAlbum,
                    artist,
                    album
            )
            artistShowable -> resources.getString(R.string.audioMetadataFormatArtist, artist)
            albumShowable -> resources.getString(R.string.audioMetadataFormatAlbum, album)
            else -> null
        }.let { TextConverter.translate(it, titleCase = true) }
    }
}