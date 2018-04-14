package com.winsonchiu.aria.dagger

import dagger.Component

@Component
interface ApplicationComponent {

    fun activityComponent(): ActivityComponent
}