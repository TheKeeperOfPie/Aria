package com.winsonchiu.aria.folder.root

import android.os.Bundle
import android.view.View
import com.winsonchiu.aria.R
import com.winsonchiu.aria.dagger.ActivityComponent
import com.winsonchiu.aria.folder.FolderFragment
import com.winsonchiu.aria.fragment.BaseFragment
import com.winsonchiu.aria.fragment.build
import com.winsonchiu.aria.util.animation.transition.DoNothingAsOverlay
import com.winsonchiu.aria.util.hasFragment

class FolderRootFragment : BaseFragment<FolderRootFragmentDaggerComponent>() {

    override fun makeComponent(activityComponent: ActivityComponent) = activityComponent.folderRootFragmentComponent()

    override fun injectSelf(component: FolderRootFragmentDaggerComponent) = component.inject(this)

    override val layoutId = R.layout.folder_root_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!childFragmentManager.hasFragment(R.id.folder_root_fragment_container)) {
            val fragment = FolderFragment.Builder().build()
            fragment.exitTransition = DoNothingAsOverlay.forSupport()
            fragment.reenterTransition = DoNothingAsOverlay.forSupport()

            childFragmentManager.beginTransaction()
                    .replace(R.id.folder_root_fragment_container, fragment)
                    .commitNow()
        }
    }
}
