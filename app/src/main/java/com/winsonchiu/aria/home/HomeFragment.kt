package com.winsonchiu.aria.home

import android.os.Bundle
import android.view.View
import com.winsonchiu.aria.R
import com.winsonchiu.aria.framework.dagger.activity.ActivityComponent
import com.winsonchiu.aria.framework.fragment.subclass.BaseFragment
import kotlinx.android.synthetic.main.home_fragment.*

class HomeFragment : BaseFragment<ActivityComponent, HomeFragmentDaggerComponent>() {

    override fun makeComponent(parentComponent: ActivityComponent) = parentComponent.homeFragmentComponent()

    override fun injectSelf(component: HomeFragmentDaggerComponent) = component.inject(this)

    override val layoutId = R.layout.home_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pager.adapter = HomePagerAdapter(view.context, childFragmentManager)
        homeTabLayout.setupWithViewPager(pager, true)
    }
}
