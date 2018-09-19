package com.winsonchiu.aria.main

import android.os.Bundle
import android.view.View
import androidx.core.view.GravityCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.winsonchiu.aria.R
import com.winsonchiu.aria.framework.dagger.activity.ActivityComponent
import com.winsonchiu.aria.framework.dagger.activity.LifecycleBoundActivity
import com.winsonchiu.aria.framework.util.dpToPx
import com.winsonchiu.aria.framework.util.hasFragment
import com.winsonchiu.aria.framework.util.mapNonNull
import com.winsonchiu.aria.home.HomeFragment
import com.winsonchiu.aria.queue.MediaQueue
import com.winsonchiu.aria.nowplaying.NowPlayingView
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : LifecycleBoundActivity() {

    @Inject
    lateinit var mediaQueue: MediaQueue

    private lateinit var viewNowPlayingBehavior: BottomSheetBehavior<NowPlayingView>

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

        viewNowPlayingBehavior = BottomSheetBehavior.from(viewNowPlaying).apply {
            isHideable = true
            state = BottomSheetBehavior.STATE_HIDDEN
            peekHeight = 72.dpToPx(this@MainActivity)
            setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(
                        bottomSheet: View,
                        slideOffset: Float
                ) {
                    viewNowPlaying.progress = 1f - slideOffset
                }

                override fun onStateChanged(
                        bottomSheet: View,
                        newState: Int
                ) {
                    when (newState) {
                        BottomSheetBehavior.STATE_COLLAPSED -> viewNowPlaying.progress = 1f
                        BottomSheetBehavior.STATE_EXPANDED -> viewNowPlaying.progress = 0f
                        BottomSheetBehavior.STATE_DRAGGING,
                        BottomSheetBehavior.STATE_HIDDEN,
                        BottomSheetBehavior.STATE_HALF_EXPANDED,
                        BottomSheetBehavior.STATE_SETTLING -> {}
                    }
                }
            })
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

    override fun onStart() {
        super.onStart()

        mediaQueue.queueUpdates
                .mapNonNull { it.currentItem }
                .map { NowPlayingView.Model(it.metadata.title, it.metadata.description, it.image) }
                .bindToLifecycle()
                .subscribe {
                    viewNowPlaying.bindData(it)
                    viewNowPlayingBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    viewNowPlayingBehavior.isHideable = false
                }
    }
}
