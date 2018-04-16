package com.winsonchiu.aria

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.winsonchiu.aria.application.AriaApplication
import com.winsonchiu.aria.dagger.ActivityComponent
import com.winsonchiu.aria.dagger.ApplicationComponent
import com.winsonchiu.aria.home.HomeFragment
import com.winsonchiu.aria.util.hasFragment

class MainActivity : AppCompatActivity() {

    companion object {
        val ACTIVITY_COMPONENT = "${MainActivity::class.java.canonicalName}.ACTIVITY_COMPONENT"
    }

    private val activityComponent by lazy { makeActivityComponent() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        if (!supportFragmentManager.hasFragment(R.id.main_activity_fragment_container)) {
            val homeFragment = HomeFragment()
            supportFragmentManager.beginTransaction()
                    .replace(R.id.main_activity_fragment_container, homeFragment)
                    .setPrimaryNavigationFragment(homeFragment)
                    .commitNow()
        }
    }

    private fun makeActivityComponent() : ActivityComponent {
        val applicationComponent = application.getSystemService(AriaApplication.APPLICATION_COMPONENT) as ApplicationComponent
        val lastActivityComponent = lastCustomNonConfigurationInstance as ActivityComponent?
        return lastActivityComponent ?: applicationComponent.activityComponent()
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
