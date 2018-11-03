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
import com.winsonchiu.aria.media.MediaBrowserConnection
import com.winsonchiu.aria.media.transport.MediaAction
import com.winsonchiu.aria.media.transport.MediaTransport
import com.winsonchiu.aria.nowplaying.NowPlayingView
import com.winsonchiu.aria.queue.MediaQueue
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : LifecycleBoundActivity() {

    override val dialogFragmentContainerId = R.id.main_activity_dialog_fragment_container

    @Inject
    lateinit var mediaQueue: MediaQueue

    @Inject
    lateinit var mediaBrowserConnection: MediaBrowserConnection

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

        viewNowPlaying.listener = object : NowPlayingView.Listener {
            override fun onClickSkipPrevious() {
                MediaTransport.send(MediaAction.SkipPrevious)
            }

            override fun onClickPlay() {
                MediaTransport.send(MediaAction.PlayPause)
            }

            override fun onClickSkipNext() {
                MediaTransport.send(MediaAction.SkipNext)
            }

            override fun onSeek(progress: Float) {
                MediaTransport.send(MediaAction.Seek(progress))
            }
        }

        viewNowPlaying.setOnClickListener {
            when (viewNowPlayingBehavior.state) {
                BottomSheetBehavior.STATE_COLLAPSED -> viewNowPlayingBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                BottomSheetBehavior.STATE_EXPANDED -> viewNowPlayingBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                BottomSheetBehavior.STATE_DRAGGING,
                BottomSheetBehavior.STATE_HIDDEN,
                BottomSheetBehavior.STATE_HALF_EXPANDED,
                BottomSheetBehavior.STATE_SETTLING -> {}
            }
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
                    viewNowPlaying.setProgress(1f - slideOffset)
                }

                override fun onStateChanged(
                        bottomSheet: View,
                        newState: Int
                ) {
                    when (newState) {
                        BottomSheetBehavior.STATE_COLLAPSED -> viewNowPlaying.setProgress(1f)
                        BottomSheetBehavior.STATE_EXPANDED -> viewNowPlaying.setProgress(0f)
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

    override fun handleBackPressed() {
        when {
            drawerMain.isDrawerOpen(GravityCompat.END) -> drawerMain.closeDrawer(GravityCompat.END)
            viewNowPlayingBehavior.state != BottomSheetBehavior.STATE_COLLAPSED
                    && viewNowPlayingBehavior.state != BottomSheetBehavior.STATE_HIDDEN -> {
                viewNowPlayingBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
            else -> super.handleBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()

        mediaQueue.queueUpdates
                .mapNonNull { it.currentEntry }
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle()
                .subscribe(viewNowPlaying::setQueueEntry)

        mediaQueue.queueUpdates
                .mapNonNull { it.currentEntry }
                .map { NowPlayingView.Model(it.metadata.title, it.metadata.description, it.image) }
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle()
                .subscribe {
                    viewNowPlaying.bindData(it)

                    if (viewNowPlayingBehavior.isHideable) {
                        viewNowPlayingBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                        viewNowPlayingBehavior.isHideable = false
                    }
                }

        mediaBrowserConnection.playbackStateChanges
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle()
                .subscribe {
                    if (it.isPresent) {
                        viewNowPlaying.setPlaybackState(it.get())
                    }
                }
    }

    override fun onResume() {
        super.onResume()
        viewNowPlaying.onResume()
    }

    override fun onPause() {
        viewNowPlaying.onPause()
        super.onPause()
    }
}
