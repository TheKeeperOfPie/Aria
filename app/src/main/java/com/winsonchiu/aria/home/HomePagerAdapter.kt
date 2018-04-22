package com.winsonchiu.aria.home

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.ViewGroup
import com.winsonchiu.aria.R
import com.winsonchiu.aria.folders.root.FolderRootFragment

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