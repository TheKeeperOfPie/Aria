package com.winsonchiu.aria.music.artwork

import android.graphics.Bitmap
import com.winsonchiu.aria.framework.dagger.ActivityScreenScope
import javax.inject.Inject

@ActivityScreenScope
class ArtworkCache(
        private val cache: MutableMap<String?, Metadata?>
) : MutableMap<String?, ArtworkCache.Metadata?> by cache {

    @Inject
    constructor() : this(mutableMapOf())

    companion object {
        val EMPTY = Metadata(null)
    }

    data class Metadata(
            val bitmap: Bitmap?
    )
}