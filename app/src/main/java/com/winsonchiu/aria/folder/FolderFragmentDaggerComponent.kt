package com.winsonchiu.aria.folder

import dagger.Subcomponent

@Subcomponent
interface FolderFragmentDaggerComponent {

    fun inject(folderFragment: FolderFragment)
}