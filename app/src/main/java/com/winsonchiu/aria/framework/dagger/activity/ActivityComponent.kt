package com.winsonchiu.aria.framework.dagger.activity

import com.winsonchiu.aria.framework.dagger.ActivityScreenScope
import com.winsonchiu.aria.framework.menu.itemsheet.view.ItemsMenuFileHeaderView
import com.winsonchiu.aria.home.HomeFragmentDaggerComponent
import com.winsonchiu.aria.main.MainActivity
import com.winsonchiu.aria.main.MainActivityViewModel
import com.winsonchiu.aria.media.MediaBrowserConnection
import com.winsonchiu.aria.queue.QueueFragmentDaggerComponent
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

    fun queueFragmentComponent(): QueueFragmentDaggerComponent

    fun inject(mainActivity: MainActivity)

    fun inject(mainActivityViewModel: MainActivityViewModel)

    fun inject(itemsMenuFileHeaderView: ItemsMenuFileHeaderView)
}

@Module
abstract class ActivityModule {

    @Binds
    @IntoSet
    @ActivityScreenScope
    abstract fun bindMediaBrowserConnection(mediaBrowserConnection: MediaBrowserConnection): ActivityLifecycleBoundComponent
}