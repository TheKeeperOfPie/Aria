package com.winsonchiu.aria

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.winsonchiu.aria.dagger.ActivityComponent
import com.winsonchiu.aria.dagger.ApplicationComponent
import com.winsonchiu.aria.home.HomeFragment
import com.winsonchiu.aria.util.hasFragment

class MainActivity : AppCompatActivity() {

    companion object {
        val ACTIVITY_COMPONENT = "${MainActivity::class.java.canonicalName}.ACTIVITY_COMPONENT"
    }

    lateinit var activityComponent: ActivityComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeActivityComponent()

        setContentView(R.layout.activity_main)

        if (!supportFragmentManager.hasFragment(R.id.main_activity_fragment_container)) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.main_activity_fragment_container, HomeFragment())
                    .commitNow()
        }
    }

    private fun initializeActivityComponent() {
        val applicationComponent = application.getSystemService(AriaApplication.APPLICATION_COMPONENT) as ApplicationComponent
        val lastActivityComponent = lastCustomNonConfigurationInstance as ActivityComponent?
        activityComponent = lastActivityComponent ?: applicationComponent.activityComponent()
    }

    override fun onRetainCustomNonConfigurationInstance() = activityComponent

    @Suppress("HasPlatformType")
    override fun getSystemService(name: String?) = when (name) {
        ACTIVITY_COMPONENT -> activityComponent
        else -> super.getSystemService(name)
    }

    @Suppress("HasPlatformType")
    override fun getSystemServiceName(serviceClass: Class<*>?) = when (serviceClass) {
        ActivityComponent::class.java -> ACTIVITY_COMPONENT
        else -> super.getSystemServiceName(serviceClass)
    }
}
