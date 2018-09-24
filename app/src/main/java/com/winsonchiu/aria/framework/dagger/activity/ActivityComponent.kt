package com.winsonchiu.aria.framework.dagger.activity

import com.winsonchiu.aria.framework.dagger.ViewInjector
import com.winsonchiu.aria.home.HomeFragmentDaggerComponent
import com.winsonchiu.aria.itemsheet.ItemsViewInjector
import com.winsonchiu.aria.main.MainActivity
import com.winsonchiu.aria.main.MainActivityViewModel
import com.winsonchiu.aria.media.MediaBrowserConnection
import com.winsonchiu.aria.queue.ui.QueueFragmentDaggerComponent
import dagger.Binds
import dagger.Module
import dagger.Subcomponent
import dagger.multibindings.IntoSet
import javax.inject.Scope

@Scope
annotation class ActivityScreenScope

@ActivityScreenScope
@Subcomponent(
        modules = [
            ActivityModule::class
        ]
)
interface ActivityComponent : ViewInjector,
        HomeFragmentDaggerComponent.ComponentProvider,
        QueueFragmentDaggerComponent.ComponentProvider,
        ItemsViewInjector {

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