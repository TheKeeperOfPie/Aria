package com.winsonchiu.aria.media

import javax.inject.Scope

@Scope
annotation class MediaScreenScope

interface MediaInjector {
    fun inject(mediaPlayer: MediaPlayer)
    fun inject(mediaService: MediaService)
    fun inject(mediaNotificationManager: MediaNotificationManager)
}