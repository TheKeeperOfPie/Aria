package com.winsonchiu.aria.queue.ui

import com.winsonchiu.aria.framework.dagger.fragment.FragmentDefaultBoundModule
import com.winsonchiu.aria.framework.dagger.fragment.FragmentLifecycleBoundComponent
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.multibindings.IntoSet
import javax.inject.Scope

@Scope
annotation class QueueFragmentScreenScope

@QueueFragmentScreenScope
@Subcomponent(
    modules = [Queue::class]
)
interface QueueFragmentDaggerComponent {

    fun inject(queueFragment: QueueFragment)

    interface ComponentProvider {
        fun queueFragmentComponent(): QueueFragmentDaggerComponent
    }
}

@Module
class Queue {

    @Provides
    @IntoSet
    @QueueFragmentScreenScope
    fun provideDefaultBoundComponent(): FragmentLifecycleBoundComponent =
        FragmentDefaultBoundModule.DefaultBoundComponent
}