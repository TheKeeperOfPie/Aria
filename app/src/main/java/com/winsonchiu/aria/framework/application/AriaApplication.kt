package com.winsonchiu.aria.framework.application

import android.os.Looper
import com.squareup.picasso.Picasso
import com.winsonchiu.aria.framework.dagger.ApplicationComponent
import com.winsonchiu.aria.framework.dagger.ApplicationModule
import com.winsonchiu.aria.framework.dagger.DaggerApplicationComponent
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.android.schedulers.AndroidSchedulers

class AriaApplication : LeakCanaryApplication() {

    companion object {
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
                        .addRequestHandler(applicationComponent.artworkRequestHandler())
                        .build()
        )
    }

    @Suppress("HasPlatformType")
    override fun getSystemService(name: String?) = when (name) {
        APPLICATION_COMPONENT -> applicationComponent
        else -> super.getSystemService(name)
    }

    @Suppress("HasPlatformType")
    override fun getSystemServiceName(serviceClass: Class<*>?) = when (serviceClass) {
        ApplicationComponent::class.java -> APPLICATION_COMPONENT
        else -> super.getSystemServiceName(serviceClass)
    }
}