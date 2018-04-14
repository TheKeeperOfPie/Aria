package com.winsonchiu.aria.home

import dagger.Subcomponent

@Subcomponent
interface HomeFragmentDaggerComponent {

    fun inject(homeFragment: HomeFragment)
}