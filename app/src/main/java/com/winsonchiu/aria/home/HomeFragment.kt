package com.winsonchiu.aria.home

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.View
import android.view.ViewGroup
import com.winsonchiu.aria.R
import com.winsonchiu.aria.dagger.ActivityComponent
import com.winsonchiu.aria.folder.root.FolderRootFragment
import com.winsonchiu.aria.fragment.BaseFragment
import kotlinx.android.synthetic.main.home_fragment.pager

class HomeFragment : BaseFragment<HomeFragmentDaggerComponent>() {

    override fun makeComponent(activityComponent: ActivityComponent) = activityComponent.homeFragmentComponent()

    override fun injectSelf(component: HomeFragmentDaggerComponent) = component.inject(this)

    override val layoutId = R.layout.home_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pager.adapter = HomePagerAdapter(view.context, childFragmentManager)
    }
}

class HomePagerAdapter(
        private val context: Context,
        private val fragmentManager: FragmentManager
) : FragmentStatePagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return FolderRootFragment()
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, item: Any) {
        super.setPrimaryItem(container, position, item)
        fragmentManager.beginTransaction()
                .setPrimaryNavigationFragment(item as Fragment)
                .commitAllowingStateLoss()
    }

    @Suppress("HasPlatformType")
    override fun getPageTitle(position: Int) = when (position) {
        0 -> R.string.page_title_folders
        else -> throw IllegalArgumentException("Invalid pager position")
    }.let(context::getString)

    override fun getCount() = 1
}