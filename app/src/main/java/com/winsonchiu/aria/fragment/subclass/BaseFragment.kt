package com.winsonchiu.aria.fragment.subclass

abstract class BaseFragment<in ParentComponent, ChildComponent> :
    ViewScopingFragment<ParentComponent, ChildComponent>() {

    @Suppress("LeakingThis")
    val TAG = this::class.java.canonicalName
}