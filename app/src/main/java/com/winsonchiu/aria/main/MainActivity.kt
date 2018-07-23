package com.winsonchiu.aria.main

import android.os.Bundle
import android.support.v4.view.GravityCompat
import com.winsonchiu.aria.R
import com.winsonchiu.aria.framework.activity.LifecycleBoundActivity
import com.winsonchiu.aria.framework.dagger.activity.ActivityComponent
import com.winsonchiu.aria.framework.util.hasFragment
import com.winsonchiu.aria.home.HomeFragment
import kotlinx.android.synthetic.main.activity_main.*

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

    override fun onBackPressed() {
        if (drawerMain.isDrawerOpen(GravityCompat.END)) {
            drawerMain.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }
}