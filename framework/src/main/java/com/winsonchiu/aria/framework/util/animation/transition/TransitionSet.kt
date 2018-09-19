package com.winsonchiu.aria.framework.util.animation.transition

import com.winsonchiu.aria.framework.BuildConfig
import com.winsonchiu.aria.framework.util.animation.transition.Constants.DEBUG_MULTIPLIER

private object Constants {
    const val DEBUG_MULTIPLIER = 1f
}

private fun Long.multiplyByDebugModifier(): Long {
    return if (BuildConfig.DEBUG) (this * DEBUG_MULTIPLIER).toLong() else this
}

open class TransitionSetFramework(
        private val defaultStartDelay: Long = -1,
        private val defaultDuration: Long = 350
) : android.transition.TransitionSet() {

    fun android.transition.Transition.addToSet(startDelay: Long = defaultStartDelay, duration: Long = defaultDuration) {
        this.setStartDelay(startDelay.multiplyByDebugModifier())
                .setDuration(duration.multiplyByDebugModifier())
                .let(::addTransition)
    }
}

open class TransitionSetSupport(
        private val defaultStartDelay: Long = -1,
        private val defaultDuration: Long = 350
) : androidx.transition.TransitionSet() {

    fun androidx.transition.Transition.addToSet(startDelay: Long = defaultStartDelay, duration: Long = defaultDuration) {
        this.setStartDelay(startDelay.multiplyByDebugModifier())
                .setDuration(duration.multiplyByDebugModifier())
                .let(::addTransition)
    }
}
