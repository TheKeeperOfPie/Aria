package com.winsonchiu.aria.media.transport

import com.jakewharton.rxrelay2.PublishRelay

object MediaTransport {

    private val actions = PublishRelay.create<MediaAction>()

    val mediaActions = actions.hide()

    fun send(action: MediaAction) {
        actions.accept(action)
    }
}