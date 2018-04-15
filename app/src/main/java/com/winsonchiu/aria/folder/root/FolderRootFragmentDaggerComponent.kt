package com.winsonchiu.aria.folder.root

import com.winsonchiu.aria.dagger.RootFragmentScreenScope
import com.winsonchiu.aria.dagger.fragment.FragmentDefaultBoundModule
import com.winsonchiu.aria.dagger.fragment.FragmentLifecycleBoundComponent
import com.winsonchiu.aria.folder.FolderFragmentDaggerComponent
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
    @RootFragmentScreenScope
    fun provideArtworkCache() = ArtworkCache()

    @Provides
    @IntoSet
    @RootFragmentScreenScope
    fun provideDefaultBoundComponent(): FragmentLifecycleBoundComponent = FragmentDefaultBoundModule.DefaultBoundComponent
}