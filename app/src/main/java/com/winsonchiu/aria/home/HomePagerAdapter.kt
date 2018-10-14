package com.winsonchiu.aria.home

import android.content.Context
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.winsonchiu.aria.R
import com.winsonchiu.aria.source.artists.ArtistsRootFragment
import com.winsonchiu.aria.source.folders.root.FolderRootFragment

class HomePagerAdapter(
        private val context: Context,
        private val fragmentManager: FragmentManager
) : FragmentStatePagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> ArtistsRootFragment()
            1 -> FolderRootFragment()
            else -> throw IllegalArgumentException("Invalid pager position")
        }
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, item: Any) {
        super.setPrimaryItem(container, position, item)
        fragmentManager.beginTransaction()
                .setPrimaryNavigationFragment(item as Fragment)
                .commitAllowingStateLoss()
    }

    @Suppress("HasPlatformType")
    override fun getPageTitle(position: Int) = when (position) {
        0 -> R.string.page_title_artists
        1 -> R.string.page_title_folders
        else -> throw IllegalArgumentException("Invalid pager position")
    }.let(context::getString)

    override fun getCount() = 2
}