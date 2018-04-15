package com.winsonchiu.aria.home

import com.winsonchiu.aria.dagger.FragmentScreenScope
import com.winsonchiu.aria.dagger.fragment.FragmentDefaultBoundModule
import dagger.Subcomponent

@FragmentScreenScope
@Subcomponent(
        modules = [FragmentDefaultBoundModule::class]
)
interface HomeFragmentDaggerComponent {

    fun inject(homeFragment: HomeFragment)
}