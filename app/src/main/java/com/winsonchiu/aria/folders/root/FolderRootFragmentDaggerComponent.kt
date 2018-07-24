package com.winsonchiu.aria.folders.root

import com.winsonchiu.aria.folders.folder.FolderFragmentDaggerComponent
import com.winsonchiu.aria.framework.dagger.RootFragmentScreenScope
import com.winsonchiu.aria.framework.dagger.fragment.FragmentDefaultBoundModule
import com.winsonchiu.aria.framework.dagger.fragment.FragmentLifecycleBoundComponent
import com.winsonchiu.aria.music.artwork.ArtworkCache
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.multibindings.IntoSet

@RootFragmentScreenScope
@Subcomponent(
        modules = [
            FolderRootFragmentModule::class
        ]
)
interface FolderRootFragmentDaggerComponent {

    fun folderFragmentComponent() : FolderFragmentDaggerComponent

    fun inject(folderRootFragment: FolderRootFragment)
}

@Module
class FolderRootFragmentModule {

    @Provides
    @IntoSet
    @RootFragmentScreenScope
    fun provideDefaultBoundComponent(): FragmentLifecycleBoundComponent = FragmentDefaultBoundModule.DefaultBoundComponent
}