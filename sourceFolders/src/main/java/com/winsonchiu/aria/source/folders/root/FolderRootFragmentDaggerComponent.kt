package com.winsonchiu.aria.source.folders.root

import com.winsonchiu.aria.framework.dagger.fragment.FragmentDefaultBoundModule
import com.winsonchiu.aria.framework.dagger.fragment.FragmentLifecycleBoundComponent
import com.winsonchiu.aria.source.folders.inner.FolderFragmentDaggerComponent
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.multibindings.IntoSet
import javax.inject.Scope

@Scope
annotation class RootFragmentScreenScope

@RootFragmentScreenScope
@Subcomponent(
        modules = [
            FolderRootFragmentModule::class
        ]
)
interface FolderRootFragmentDaggerComponent {

    fun folderFragmentComponent() : FolderFragmentDaggerComponent

    fun inject(folderRootFragment: FolderRootFragment)

    interface ComponentProvider {
        fun folderRootFragmentComponent(): FolderRootFragmentDaggerComponent
    }
}

@Module
class FolderRootFragmentModule {

    @Provides
    @IntoSet
    @RootFragmentScreenScope
    fun provideDefaultBoundComponent(): FragmentLifecycleBoundComponent = FragmentDefaultBoundModule.DefaultBoundComponent
}