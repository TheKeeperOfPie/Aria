package com.winsonchiu.aria.fragment

import com.winsonchiu.aria.dagger.ActivityComponent

interface InjectableFragment<DaggerComponent> {

    fun makeComponent(activityComponent: ActivityComponent): DaggerComponent

    fun injectSelf(component: DaggerComponent)
}