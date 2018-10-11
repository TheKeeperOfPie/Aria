package com.winsonchiu.aria.source.artists

import android.net.Uri

inline class ArtistId(val value: String)
inline class ArtistKey(val value: String)

data class Artist(
        val id: ArtistId,
        val name: String?,
        val image: Uri?,
        val key: ArtistKey?,
        val albumCount: Int,
        val trackCount: Int
)