package com.winsonchiu.aria.music.artwork

import android.graphics.Bitmap
import com.winsonchiu.aria.framework.dagger.ActivityScreenScope
import javax.inject.Inject

@ActivityScreenScope
class ArtworkCache private constructor(
        private val cache: MutableMap<String?, Metadata?>
) : MutableMap<String?, ArtworkCache.Metadata?> by cache {

    @Inject
    constructor() : this(mutableMapOf())

    companion object {
        private val EMPTY = Metadata(null)
    }

    private data class Metadata(
            val bitmap: Bitmap?
    )
}