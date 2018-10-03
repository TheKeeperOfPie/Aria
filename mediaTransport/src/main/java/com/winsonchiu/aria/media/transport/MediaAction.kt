package com.winsonchiu.aria.media.transport

sealed class MediaAction {

    object Play : MediaAction()
    object Pause : MediaAction()
    object PlayPause : MediaAction()
    object SkipPrevious : MediaAction()
    object SkipNext : MediaAction()
    data class Seek(val progress: Float) : MediaAction()
}