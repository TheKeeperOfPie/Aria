package com.winsonchiu.aria.home

import com.winsonchiu.aria.framework.dagger.fragment.FragmentDefaultBoundModule
import com.winsonchiu.aria.framework.dagger.fragment.FragmentLifecycleBoundComponent
import com.winsonchiu.aria.source.artists.ArtistsFragmentDaggerComponent
import com.winsonchiu.aria.source.folders.root.FolderRootFragmentDaggerComponent
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.multibindings.IntoSet
import javax.inject.Scope

@Scope
annotation class HomeFragmentScreenScope

@HomeFragmentScreenScope
@Subcomponent(
        modules = [HomeFragmentModule::class]
)
interface HomeFragmentDaggerComponent : ArtistsFragmentDaggerComponent.ComponentProvider, FolderRootFragmentDaggerComponent.ComponentProvider {

    fun inject(homeFragment: HomeFragment)

    interface ComponentProvider {
        fun homeFragmentComponent(): HomeFragmentDaggerComponent
    }
}

@Module
class HomeFragmentModule {

    @Provides
    @IntoSet
    @HomeFragmentScreenScope
    fun provideDefaultBoundComponent(): FragmentLifecycleBoundComponent = FragmentDefaultBoundModule.DefaultBoundComponent
}