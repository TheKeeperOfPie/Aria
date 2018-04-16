package com.winsonchiu.aria.fragment.subclass

import androidx.core.view.postDelayed

abstract class BaseFragment<in ParentComponent, ChildComponent> :
    ViewScopingFragment<ParentComponent, ChildComponent>() {

    @Suppress("LeakingThis")
    val TAG = this::class.java.canonicalName

    fun postponeEnterTransition(delay: Long) {
        view?.let {
            postponeEnterTransition()
            it.postDelayed(delay) {
                startPostponedEnterTransition()
            }
        }
    }
}