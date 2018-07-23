package com.winsonchiu.aria.home

import com.winsonchiu.aria.framework.dagger.HomeFragmentScreenScope
import com.winsonchiu.aria.framework.dagger.fragment.FragmentDefaultBoundModule
import com.winsonchiu.aria.framework.dagger.fragment.FragmentLifecycleBoundComponent
import com.winsonchiu.aria.folders.root.FolderRootFragmentDaggerComponent
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.multibindings.IntoSet

@HomeFragmentScreenScope
@Subcomponent(
        modules = [HomeFragmentModule::class]
)
interface HomeFragmentDaggerComponent {

    fun folderRootFragmentComponent(): FolderRootFragmentDaggerComponent

    fun inject(homeFragment: HomeFragment)
}

@Module
class HomeFragmentModule {

    @Provides
    @IntoSet
    @HomeFragmentScreenScope
    fun provideDefaultBoundComponent(): FragmentLifecycleBoundComponent = FragmentDefaultBoundModule.DefaultBoundComponent
}