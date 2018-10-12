package com.winsonchiu.aria.artwork.lastfm.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LastFmArtistInfo(
        val artist: Artist
) {

    @JsonClass(generateAdapter = true)
    data class Artist(
            val name: String?,
            @Json(name = "url") val lastFmUrl: String?,
            @Json(name = "image") val images: List<Image>
    ) {

        @JsonClass(generateAdapter = true)
        data class Image(
                @Json(name = "#text") val url: String,
                val size: Size?
        ) {

            enum class Size {
                @Json(name = "small")
                SMALL,
                @Json(name = "medium")
                MEDIUM,
                @Json(name = "large")
                LARGE,
                @Json(name = "extralarge")
                EXTRA_LARGE,
                @Json(name = "mega")
                MEGA
            }
        }
    }
}