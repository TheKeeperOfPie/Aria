package com.winsonchiu.aria.folder

import android.os.Bundle
import android.view.View
import com.winsonchiu.aria.R
import com.winsonchiu.aria.dagger.ActivityComponent
import com.winsonchiu.aria.fragment.BaseFragment
import com.winsonchiu.aria.fragment.FragmentInitializer
import com.winsonchiu.aria.fragment.arg
import kotlinx.android.synthetic.main.folder_fragment.folderRecyclerView

class FolderFragment : BaseFragment<FolderFragmentDaggerComponent>() {

    override fun makeComponent(activityComponent: ActivityComponent) = activityComponent.folderFragmentComponent()

    override fun injectSelf(component: FolderFragmentDaggerComponent) = component.inject(this)

    object Args : Builder()
    open class Builder : FragmentInitializer<FolderFragment>({ FolderFragment() }) {
        val folder = string("folder")
    }

    override val layoutId = R.layout.folder_fragment

    val folder by arg(Args.folder)

    var epoxyController by ViewScoped(FolderEpoxyController())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        folderRecyclerView.setController(epoxyController)
    }
}