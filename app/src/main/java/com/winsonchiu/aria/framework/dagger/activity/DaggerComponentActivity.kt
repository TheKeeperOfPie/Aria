package com.winsonchiu.aria.framework.dagger.activity

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import com.winsonchiu.aria.framework.application.AriaApplication
import com.winsonchiu.aria.framework.dagger.ApplicationComponent
import com.winsonchiu.aria.framework.dagger.activity.DaggerConstants.ACTIVITY_COMPONENT

abstract class DaggerComponentActivity : AppCompatActivity() {

    private val activityComponent by lazy { makeActivityComponent() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectSelf(activityComponent)
    }

    abstract fun injectSelf(activityComponent: ActivityComponent)

    private fun makeActivityComponent() : ActivityComponent {
        val applicationComponent = application.getSystemService(AriaApplication.APPLICATION_COMPONENT) as ApplicationComponent
        val lastActivityComponent = lastCustomNonConfigurationInstance as ActivityComponent?
        return lastActivityComponent ?: applicationComponent.activityComponent()
    }

    final override fun onRetainCustomNonConfigurationInstance() = activityComponent

    @CallSuper
    @Suppress("HasPlatformType")
    override fun getSystemService(name: String) = when (name) {
        DaggerConstants.ACTIVITY_COMPONENT -> activityComponent
        else -> super.getSystemService(name)
    }

    @CallSuper
    @Suppress("HasPlatformType")
    override fun getSystemServiceName(serviceClass: Class<*>) = when (serviceClass) {
        ActivityComponent::class.java -> ACTIVITY_COMPONENT
        else -> super.getSystemServiceName(serviceClass)
    }
}