package com.winsonchiu.aria.media

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ServiceLifecycleDispatcher
import android.content.Intent
import android.os.IBinder
import android.support.annotation.CallSuper
import android.support.v4.media.MediaBrowserServiceCompat
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.ObservableSubscribeProxy
import com.uber.autodispose.SingleSubscribeProxy
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import io.reactivex.Observable
import io.reactivex.Single

abstract class LifecycleMediaBrowserService : MediaBrowserServiceCompat(), LifecycleOwner {

    private val dispatcher = ServiceLifecycleDispatcher(this)

    @CallSuper
    override fun onCreate() {
        dispatcher.onServicePreSuperOnCreate()
        super.onCreate()
    }

    @CallSuper
    override fun onBind(intent: Intent): IBinder? {
        dispatcher.onServicePreSuperOnBind()
        return super.onBind(intent)
    }

    @CallSuper
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        dispatcher.onServicePreSuperOnStart()
        return super.onStartCommand(intent, flags, startId)
    }

    @CallSuper
    override fun onDestroy() {
        dispatcher.onServicePreSuperOnDestroy()
        super.onDestroy()
    }

    override fun getLifecycle() = dispatcher.lifecycle

    fun <T> Single<T>.bindToLifecycle(): SingleSubscribeProxy<T> {
        return `as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this@LifecycleMediaBrowserService)))
    }

    fun <T> Observable<T>.bindToLifecycle(): ObservableSubscribeProxy<T> {
        return `as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this@LifecycleMediaBrowserService)))
    }
}