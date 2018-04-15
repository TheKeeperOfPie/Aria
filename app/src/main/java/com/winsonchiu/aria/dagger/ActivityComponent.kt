package com.winsonchiu.aria.dagger

import com.winsonchiu.aria.folder.root.FolderRootFragmentDaggerComponent
import com.winsonchiu.aria.home.HomeFragmentDaggerComponent
import dagger.Subcomponent

@Subcomponent
interface ActivityComponent {

    fun homeFragmentComponent(): HomeFragmentDaggerComponent
    fun folderRootFragmentComponent(): FolderRootFragmentDaggerComponent
}