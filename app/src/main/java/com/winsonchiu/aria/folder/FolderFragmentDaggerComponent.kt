package com.winsonchiu.aria.folder

import com.winsonchiu.aria.dagger.FragmentScreenScope
import com.winsonchiu.aria.dagger.fragment.FragmentLifecycleBoundComponent
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.multibindings.IntoSet

@FragmentScreenScope
@Subcomponent(
        modules = [FolderFragmentModule::class]
)
interface FolderFragmentDaggerComponent {

    fun inject(folderFragment: FolderFragment)
}

@Module
class FolderFragmentModule {

    @Provides
    @IntoSet
    @FragmentScreenScope
    fun bindFolderController(folderController: FolderController): FragmentLifecycleBoundComponent = folderController
}