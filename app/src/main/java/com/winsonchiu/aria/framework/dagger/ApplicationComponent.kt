package com.winsonchiu.aria.framework.dagger

import com.winsonchiu.aria.artwork.ArtworkRequestHandler
import com.winsonchiu.aria.artwork.ArtworkScope
import com.winsonchiu.aria.framework.dagger.activity.ActivityComponent
import com.winsonchiu.aria.media.MediaPlayer
import com.winsonchiu.aria.media.MediaService
import com.winsonchiu.aria.queue.QueueScope
import com.winsonchiu.aria.source.folder.FolderScope
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
interface ApplicationComponent {

    @Component.Builder
    interface Builder {

        fun applicationModule(applicationModule: ApplicationModule): Builder

        fun build(): ApplicationComponent
    }

    fun activityComponent(): ActivityComponent

    fun inject(mediaService: MediaService)

    fun inject(mediaPlayer: MediaPlayer)

    fun artworkRequestHandler(): ArtworkRequestHandler
}