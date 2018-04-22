package com.winsonchiu.aria.activity

import android.os.Bundle
import com.winsonchiu.aria.R
import com.winsonchiu.aria.dagger.activity.ActivityComponent
import com.winsonchiu.aria.home.HomeFragment
import com.winsonchiu.aria.util.hasFragment

class MainActivity : LifecycleBoundActivity() {

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

    override fun injectSelf(activityComponent: ActivityComponent) = activityComponent.inject(this)
}
