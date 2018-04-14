package com.winsonchiu.aria.folder.root

import dagger.Subcomponent

@Subcomponent
interface FolderRootFragmentDaggerComponent {

    fun inject(folderRootFragment: FolderRootFragment)
}