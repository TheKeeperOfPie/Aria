package com.winsonchiu.aria.framework.dagger

import com.winsonchiu.aria.nowplaying.NowPlayingView

interface ViewInjector {

    fun inject(view: NowPlayingView)
}