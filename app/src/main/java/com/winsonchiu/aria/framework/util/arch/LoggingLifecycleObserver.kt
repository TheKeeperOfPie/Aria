package com.winsonchiu.aria.framework.util.arch

import android.arch.lifecycle.DefaultLifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.support.annotation.CallSuper
import android.util.Log

interface LoggingLifecycleObserver : DefaultLifecycleObserver {

    object Default : LoggingLifecycleObserver

    fun getCustomTag() = null as String?

    @CallSuper
    override fun onCreate(owner: LifecycleOwner) {
        Log.v(getCustomTag() ?: owner::class.java.simpleName, "onCreate called with owner = $owner")
    }

    @CallSuper
    override fun onStart(owner: LifecycleOwner) {
        Log.v(getCustomTag() ?: owner::class.java.simpleName, "onStart called with owner = $owner")
    }

    @CallSuper
    override fun onResume(owner: LifecycleOwner) {
        Log.v(getCustomTag() ?: owner::class.java.simpleName, "onResume called with owner = $owner")
    }

    @CallSuper
    override fun onPause(owner: LifecycleOwner) {
        Log.v(getCustomTag() ?: owner::class.java.simpleName, "onPause called with owner = $owner")
    }

    @CallSuper
    override fun onStop(owner: LifecycleOwner) {
        Log.v(getCustomTag() ?: owner::class.java.simpleName, "onStop called with owner = $owner")
    }

    @CallSuper
    override fun onDestroy(owner: LifecycleOwner) {
        Log.v(getCustomTag() ?: owner::class.java.simpleName, "onDestroy called with owner = $owner")
    }
}