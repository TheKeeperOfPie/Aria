package com.winsonchiu.aria.framework.application

import android.os.Looper
import android.os.StrictMode
import com.squareup.picasso.LruCache
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.winsonchiu.aria.framework.dagger.ApplicationComponent
import com.winsonchiu.aria.framework.dagger.ApplicationModule
import com.winsonchiu.aria.framework.dagger.DaggerApplicationComponent
import com.winsonchiu.aria.main.MainActivity
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.android.schedulers.AndroidSchedulers

class AriaApplication : LeakCanaryApplication() {

    companion object {
        private val PICASSO_DISK_CACHE_SIZE = 500L * 1024 * 1024
        private val PICASSO_MEMORY_CACHE_SIZE = 250 * 1024 * 1024

        val APPLICATION_COMPONENT = "${AriaApplication::class.java.canonicalName}.APPLICATION_COMPONENT"
    }

    private val applicationComponent = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .build()

    override fun onCreateApp() {
        super.onCreateApp()

        RxAndroidPlugins.setInitMainThreadSchedulerHandler {
            AndroidSchedulers.from(Looper.getMainLooper(), true)
        }

        Picasso.setSingletonInstance(
                Picasso.Builder(this)
                        .memoryCache(LruCache(PICASSO_MEMORY_CACHE_SIZE))
                        .downloader(OkHttp3Downloader(this, PICASSO_DISK_CACHE_SIZE))
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
        APPLICATION_COMPONENT -> applicationComponent
        else -> super.getSystemService(name)
    }

    @Suppress("HasPlatformType")
    override fun getSystemServiceName(serviceClass: Class<*>) = when (serviceClass) {
        ApplicationComponent::class.java -> APPLICATION_COMPONENT
        else -> super.getSystemServiceName(serviceClass)
    }
}