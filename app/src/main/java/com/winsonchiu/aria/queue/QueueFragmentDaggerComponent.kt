package com.winsonchiu.aria.queue

import com.winsonchiu.aria.framework.dagger.QueueFragmentScreenScope
import com.winsonchiu.aria.framework.dagger.fragment.FragmentDefaultBoundModule
import com.winsonchiu.aria.framework.dagger.fragment.FragmentLifecycleBoundComponent
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.multibindings.IntoSet

@QueueFragmentScreenScope
@Subcomponent(
    modules = [Queue::class]
)
interface QueueFragmentDaggerComponent {

    fun inject(queueFragment: QueueFragment)
}

@Module
class Queue {

    @Provides
    @IntoSet
    @QueueFragmentScreenScope
    fun provideDefaultBoundComponent(): FragmentLifecycleBoundComponent =
        FragmentDefaultBoundModule.DefaultBoundComponent
}