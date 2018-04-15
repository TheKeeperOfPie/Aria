package com.winsonchiu.aria.dagger

import dagger.Component

@ApplicationScope
@Component(
        modules = [
            ApplicationModule::class
        ]
)
interface ApplicationComponent {

    @Component.Builder
    interface Builder {

        fun applicationModule(applicationModule: ApplicationModule): Builder

        fun build(): ApplicationComponent
    }

    fun activityComponent(): ActivityComponent
}