package com.winsonchiu.aria.folder.root

import com.winsonchiu.aria.dagger.FragmentScreenScope
import com.winsonchiu.aria.dagger.fragment.FragmentDefaultBoundModule
import dagger.Subcomponent

@FragmentScreenScope
@Subcomponent(
        modules = [FragmentDefaultBoundModule::class]
)
interface FolderRootFragmentDaggerComponent {

    fun inject(folderRootFragment: FolderRootFragment)
}