package com.winsonchiu.aria.framework.dagger

import com.winsonchiu.aria.artwork.ArtworkRequestHandler
import com.winsonchiu.aria.artwork.ArtworkScope
import com.winsonchiu.aria.framework.dagger.activity.ActivityComponent
import com.winsonchiu.aria.media.MediaInjector
import com.winsonchiu.aria.queue.QueueScope
import com.winsonchiu.aria.source.folders.FolderScope
import dagger.Component
import javax.inject.Scope

@Scope
annotation class ApplicationScope

@FolderScope
@QueueScope
@ArtworkScope
@ApplicationScope
@Component(
        modules = [
            ApplicationModule::class
        ]
)
interface ApplicationComponent : MediaInjector {

    @Component.Builder
    interface Builder {

        fun applicationModule(applicationModule: ApplicationModule): Builder

        fun build(): ApplicationComponent
    }

    fun activityComponent(): ActivityComponent

    fun artworkRequestHandler(): ArtworkRequestHandler
}