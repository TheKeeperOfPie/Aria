package com.winsonchiu.aria.util

import android.view.animation.Animation

abstract class AnimationListenerAdapter : Animation.AnimationListener {

    override fun onAnimationRepeat(animation: Animation?) {}

    override fun onAnimationEnd(animation: Animation?) {}

    override fun onAnimationStart(animation: Animation?) {}
}