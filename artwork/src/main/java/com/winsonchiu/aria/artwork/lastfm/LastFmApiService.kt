package com.winsonchiu.aria.artwork.lastfm

import com.winsonchiu.aria.artwork.lastfm.model.LastFmArtistInfo
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface LastFmApiService {

    @GET("?method=artist.getInfo&format=json&autocorrect=1")
    fun artist(
            @Query("artist") name: String,
            @Query("lang") lang: String
    ): Call<LastFmArtistInfo>
}