package com.winsonchiu.aria

import android.app.Application
import com.winsonchiu.aria.dagger.ApplicationComponent
import com.winsonchiu.aria.dagger.DaggerApplicationComponent

class AriaApplication : Application() {

    companion object {
        val APPLICATION_COMPONENT = "${AriaApplication::class.java.canonicalName}.APPLICATION_COMPONENT"
    }

    private val applicationComponent = DaggerApplicationComponent.create()

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