package com.winsonchiu.aria.framework.application

import android.os.Looper
import android.os.StrictMode
import com.squareup.picasso.LruCache
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.winsonchiu.aria.BuildConfig
import com.winsonchiu.aria.framework.dagger.ApplicationComponent
import com.winsonchiu.aria.framework.dagger.ApplicationModule
import com.winsonchiu.aria.framework.dagger.DaggerApplicationComponent
import com.winsonchiu.aria.framework.dagger.activity.DaggerConstants
import com.winsonchiu.aria.main.MainActivity
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.android.schedulers.AndroidSchedulers
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File

class AriaApplication : LeakCanaryApplication() {

    companion object {
        private const val PICASSO_DISK_CACHE_SIZE = 500L * 1024 * 1024
        private const val PICASSO_MEMORY_CACHE_SIZE = 250 * 1024 * 1024
    }

    private val applicationComponent = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .build()

    override fun onCreateApp() {
        super.onCreateApp()

        RxAndroidPlugins.setInitMainThreadSchedulerHandler {
            AndroidSchedulers.from(Looper.getMainLooper(), true)
        }


        val cache = File(cacheDir, "picasso-cache")
        val okHttpClient = OkHttpClient.Builder()
                .cache(Cache(cache, PICASSO_DISK_CACHE_SIZE))
                .build()
        val downloader = OkHttp3Downloader(okHttpClient)

        Picasso.setSingletonInstance(
                Picasso.Builder(this)
                        .indicatorsEnabled(BuildConfig.DEBUG)
                        .memoryCache(LruCache(PICASSO_MEMORY_CACHE_SIZE))
                        .downloader(downloader)
                        .addRequestHandler(applicationComponent.artworkRequestHandler())
                        .build()
        )
    }

    override fun enableStrictMode() {
        super.enableStrictMode()

        // To fix a quirk with rotating multiple times quickly, increment the expected Activity count
        val incrementExpectedActivityCount = StrictMode::class.java.getMethod("incrementExpectedActivityCount", Class::class.java)
        incrementExpectedActivityCount.invoke(null, MainActivity::class.java)
        incrementExpectedActivityCount.invoke(null, MainActivity::class.java)
        incrementExpectedActivityCount.invoke(null, MainActivity::class.java)
    }

    @Suppress("HasPlatformType")
    override fun getSystemService(name: String) = when (name) {
        DaggerConstants.APPLICATION_COMPONENT -> applicationComponent
        else -> super.getSystemService(name)
    }

    @Suppress("HasPlatformType")
    override fun getSystemServiceName(serviceClass: Class<*>) = when (serviceClass) {
        ApplicationComponent::class.java -> DaggerConstants.APPLICATION_COMPONENT
        else -> super.getSystemServiceName(serviceClass)
    }
}