package com.winsonchiu.aria.home

import android.content.Context
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import butterknife.BindView
import com.winsonchiu.aria.R
import com.winsonchiu.aria.dagger.ActivityComponent
import com.winsonchiu.aria.folder.root.FolderRootFragment
import com.winsonchiu.aria.fragment.BaseFragment

class HomeFragment : BaseFragment<HomeFragmentDaggerComponent>() {

    override fun makeComponent(activityComponent: ActivityComponent) = activityComponent.homeFragmentComponent()

    override fun injectSelf(component: HomeFragmentDaggerComponent) = component.inject(this)

    override val layoutId = R.layout.home_fragment

    @BindView(R.id.app_bar_layout)
    lateinit var appBarLayout: AppBarLayout

    @BindView(R.id.pager)
    lateinit var pager: ViewPager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pager.adapter = HomePagerAdapter(view.context, childFragmentManager)
    }
}

class HomePagerAdapter(
        private val context: Context,
        fragmentManager: FragmentManager
) : FragmentStatePagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return FolderRootFragment()
    }

    @Suppress("HasPlatformType")
    override fun getPageTitle(position: Int) = when (position) {
        0 -> R.string.page_title_folders
        else -> throw IllegalArgumentException("Invalid pager position")
    }.let(context::getString)

    override fun getCount() = 1
}