package com.winsonchiu.aria.dagger

import com.winsonchiu.aria.dagger.activity.ActivityComponent
import com.winsonchiu.aria.media.MediaPlayer
import com.winsonchiu.aria.media.MediaService
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

    fun inject(mediaService: MediaService)

    fun inject(mediaPlayer: MediaPlayer)
}