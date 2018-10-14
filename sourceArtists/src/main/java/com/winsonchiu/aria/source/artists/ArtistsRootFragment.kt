package com.winsonchiu.aria.source.artists

import android.os.Bundle
import android.view.View
import com.winsonchiu.aria.framework.fragment.subclass.BaseFragment
import com.winsonchiu.aria.framework.util.hasFragment
import com.winsonchiu.aria.source.artists.artists.ArtistsFragment

class ArtistsRootFragment : BaseFragment<ArtistsRootFragmentDaggerComponent.ComponentProvider, ArtistsRootFragmentDaggerComponent>() {

    override val layoutId = R.layout.artists_root_fragment

    override fun makeComponent(parentComponent: ArtistsRootFragmentDaggerComponent.ComponentProvider) = parentComponent.artistsRootFragmentComponent()

    override fun injectSelf(component: ArtistsRootFragmentDaggerComponent) = component.inject(this)

    override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        if (!childFragmentManager.hasFragment(R.id.artistsRootFragmentContainer)) {
            childFragmentManager.beginTransaction()
                    .replace(R.id.artistsRootFragmentContainer, ArtistsFragment())
                    .commitNow()
        }
    }
}