package com.winsonchiu.aria.artwork.lastfm

import android.app.Application
import android.preference.PreferenceManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.EnumJsonAdapter
import com.winsonchiu.aria.artwork.ArtworkScope
import com.winsonchiu.aria.artwork.BuildConfig
import com.winsonchiu.aria.artwork.lastfm.model.LastFmArtistInfo
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Inject

@ArtworkScope
class LastFmApi constructor(
        lastFmApiService: LastFmApiService
) : LastFmApiService by lastFmApiService {

    @Inject
    constructor(
            application: Application
    ) : this(Unit.run {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
        val apiKey = sharedPreferences.getString("lastFmApiKey", "")

        if (apiKey.isNullOrEmpty()) {
            sharedPreferences.edit().putString("lastFmApiKey", "API_KEY").commit()
        }

        val okHttpClient = OkHttpClient.Builder()
                .addNetworkInterceptor {
                    if (apiKey.isNullOrEmpty() || apiKey == "API_KEY") {
                        throw IllegalArgumentException()
                    }

                    val url = it.request()
                            .url()
                            .newBuilder()
                            .addQueryParameter("api_key", apiKey)
                            .build()

                    val request = it.request()
                            .newBuilder()
                            .url(url)
                            .build()

                    it.proceed(request)
                }
                .addNetworkInterceptor(
                        HttpLoggingInterceptor()
                                .setLevel(HttpLoggingInterceptor.Level.BODY)
                )
                .build()

        val moshi = Moshi.Builder()
                .add(
                        LastFmArtistInfo.Artist.Image.Size::class.java,
                        EnumJsonAdapter.create(LastFmArtistInfo.Artist.Image.Size::class.java)
                                .withUnknownFallback(null)
                )
                .build()

        Retrofit.Builder()
                .baseUrl("https://ws.audioscrobbler.com/2.0/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .validateEagerly(BuildConfig.DEBUG)
                .client(okHttpClient)
                .build()
                .create(LastFmApiService::class.java)
    })
}