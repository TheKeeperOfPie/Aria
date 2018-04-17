package com.winsonchiu.aria.fragment.subclass

import androidx.core.view.postDelayed
import java.util.UUID

abstract class BaseFragment<in ParentComponent, ChildComponent> :
    ViewScopingFragment<ParentComponent, ChildComponent>() {

    @Suppress("LeakingThis")
    val TAG = this::class.java.canonicalName

    val uniqueTransitionName = UUID.randomUUID().toString()

    fun postponeEnterTransition(delay: Long) {
        view?.let {
            postponeEnterTransition()
            it.postDelayed(delay) {
                startPostponedEnterTransition()
            }
        }
    }
}