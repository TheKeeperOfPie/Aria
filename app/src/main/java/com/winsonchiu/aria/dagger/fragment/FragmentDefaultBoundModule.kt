package com.winsonchiu.aria.dagger.fragment

import com.winsonchiu.aria.dagger.FragmentScreenScope
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Module
class FragmentDefaultBoundModule {

    object DefaultBoundComponent : FragmentLifecycleBoundComponent()

    @Provides
    @IntoSet
    @FragmentScreenScope
    fun provideDefaultBoundComponent(): FragmentLifecycleBoundComponent = DefaultBoundComponent
}