package com.winsonchiu.aria.source.folders.inner

import com.winsonchiu.aria.framework.dagger.fragment.FragmentLifecycleBoundComponent
import dagger.Binds
import dagger.Module
import dagger.Subcomponent
import dagger.multibindings.IntoSet
import javax.inject.Scope

@Scope
annotation class FolderFragmentScope

@FolderFragmentScope
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
    @FolderFragmentScope
    abstract fun bindFolderController(folderController: FolderController): FragmentLifecycleBoundComponent
}