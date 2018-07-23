package com.winsonchiu.aria.framework.activity

import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.v7.app.AppCompatActivity
import com.winsonchiu.aria.framework.application.AriaApplication
import com.winsonchiu.aria.framework.dagger.ApplicationComponent
import com.winsonchiu.aria.framework.dagger.activity.ActivityComponent
import com.winsonchiu.aria.main.MainActivity

abstract class DaggerComponentActivity : AppCompatActivity() {

    companion object {
        val ACTIVITY_COMPONENT = "${MainActivity::class.java.canonicalName}.ACTIVITY_COMPONENT"
    }

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
    override fun getSystemService(name: String?) = when (name) {
        ACTIVITY_COMPONENT -> activityComponent
        else -> super.getSystemService(name)
    }

    @CallSuper
    @Suppress("HasPlatformType")
    override fun getSystemServiceName(serviceClass: Class<*>?) = when (serviceClass) {
        ActivityComponent::class.java -> ACTIVITY_COMPONENT
        else -> super.getSystemServiceName(serviceClass)
    }
}