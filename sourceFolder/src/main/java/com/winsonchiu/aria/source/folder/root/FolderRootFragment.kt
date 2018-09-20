package com.winsonchiu.aria.source.folder.root

import android.os.Bundle
import android.os.Environment
import android.view.View
import com.winsonchiu.aria.framework.fragment.build
import com.winsonchiu.aria.framework.fragment.subclass.BaseFragment
import com.winsonchiu.aria.framework.util.hasFragment
import com.winsonchiu.aria.source.folder.R
import com.winsonchiu.aria.source.folder.inner.FolderFragment
import com.winsonchiu.aria.source.folder.inner.FolderToFolderTransition

class FolderRootFragment : BaseFragment<FolderRootFragmentDaggerComponent.ComponentProvider, FolderRootFragmentDaggerComponent>() {

    override fun makeComponent(parentComponent: FolderRootFragmentDaggerComponent.ComponentProvider) = parentComponent.folderRootFragmentComponent()

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
