package com.winsonchiu.aria.music.artwork

import android.graphics.Bitmap

class ArtworkCache(
        private val cache: MutableMap<String?, Metadata?> = mutableMapOf()
) : MutableMap<String?, ArtworkCache.Metadata?> by cache {

    companion object {
        val EMPTY = Metadata(null)
    }

    data class Metadata(
            val bitmap: Bitmap?
    )
}