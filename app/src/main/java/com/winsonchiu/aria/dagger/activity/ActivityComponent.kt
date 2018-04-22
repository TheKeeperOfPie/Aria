package com.winsonchiu.aria.dagger.activity

import com.winsonchiu.aria.activity.MainActivity
import com.winsonchiu.aria.activity.MainActivityViewModel
import com.winsonchiu.aria.dagger.ActivityScreenScope
import com.winsonchiu.aria.home.HomeFragmentDaggerComponent
import com.winsonchiu.aria.media.MediaBrowserConnection
import dagger.Binds
import dagger.Module
import dagger.Subcomponent
import dagger.multibindings.IntoSet

@ActivityScreenScope
@Subcomponent(
        modules = [
            ActivityModule::class
        ]
)
interface ActivityComponent {

    fun homeFragmentComponent(): HomeFragmentDaggerComponent

    fun inject(mainActivity: MainActivity)

    fun inject(mainActivityViewModel: MainActivityViewModel)
}

@Module
abstract class ActivityModule {

    @Binds
    @IntoSet
    @ActivityScreenScope
    abstract fun bindMediaBrowserConnection(mediaBrowserConnection: MediaBrowserConnection): ActivityLifecycleBoundComponent
}