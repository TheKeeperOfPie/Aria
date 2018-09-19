package com.winsonchiu.aria.framework.dagger.fragment

import dagger.Module
import javax.inject.Scope

@Module
class FragmentDefaultBoundModule {

    object DefaultBoundComponent : FragmentLifecycleBoundComponent()
}