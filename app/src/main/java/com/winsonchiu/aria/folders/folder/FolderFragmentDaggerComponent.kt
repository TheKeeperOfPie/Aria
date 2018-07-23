package com.winsonchiu.aria.folders.folder

import com.winsonchiu.aria.framework.dagger.FragmentScreenScope
import com.winsonchiu.aria.framework.dagger.fragment.FragmentLifecycleBoundComponent
import dagger.Binds
import dagger.Module
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
abstract class FolderFragmentModule {

    @Binds
    @IntoSet
    @FragmentScreenScope
    abstract fun bindFolderController(folderController: FolderController): FragmentLifecycleBoundComponent
}