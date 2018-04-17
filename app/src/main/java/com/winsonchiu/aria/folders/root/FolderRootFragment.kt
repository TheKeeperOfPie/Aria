package com.winsonchiu.aria.folders.root

import android.os.Bundle
import android.os.Environment
import android.view.View
import com.winsonchiu.aria.R
import com.winsonchiu.aria.folders.folder.FolderFragment
import com.winsonchiu.aria.folders.folder.FolderToFolderTransition
import com.winsonchiu.aria.fragment.build
import com.winsonchiu.aria.fragment.subclass.BaseFragment
import com.winsonchiu.aria.home.HomeFragmentDaggerComponent
import com.winsonchiu.aria.util.hasFragment

class FolderRootFragment : BaseFragment<HomeFragmentDaggerComponent, FolderRootFragmentDaggerComponent>() {

    override fun makeComponent(parentComponent: HomeFragmentDaggerComponent) = parentComponent.folderRootFragmentComponent()

    override fun injectSelf(component: FolderRootFragmentDaggerComponent) = component.inject(this)

    override val layoutId = R.layout.folder_root_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!childFragmentManager.hasFragment(R.id.folder_root_fragment_container)) {
            buildAndCommitInitialBackStack()
        }
    }

    private fun buildAndCommitInitialBackStack() {
        val musicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        val directories = mutableListOf(musicDirectory)

        var currentFile = musicDirectory.parentFile
        while (currentFile?.canRead() == true) {
            directories.add(currentFile)
            currentFile = currentFile.parentFile
        }

        directories.reversed()
                .forEachIndexed { index, file ->
                    val fragment = FolderFragment.Builder().build {
                        folder put file.absolutePath
                    }

                    FolderToFolderTransition.applyToFragment(fragment)

                    childFragmentManager.beginTransaction()
                            .replace(R.id.folder_root_fragment_container, fragment)
                            .apply {
                                if (index > 0) {
                                    addToBackStack(file.absolutePath)
                                }
                            }
                            .commit()
                }
    }
}
