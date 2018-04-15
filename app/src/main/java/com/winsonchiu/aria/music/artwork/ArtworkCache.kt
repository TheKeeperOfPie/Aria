package com.winsonchiu.aria.music.artwork

import android.graphics.Bitmap

class ArtworkCache(
        private val cache: MutableMap<String?, Bitmap?> = mutableMapOf()
) : MutableMap<String?, Bitmap?> by cache