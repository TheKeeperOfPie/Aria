package com.winsonchiu.aria.dagger.activity

import android.support.annotation.CallSuper
import android.support.annotation.MainThread
import com.jakewharton.rxrelay2.PublishRelay
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.FlowableSubscribeProxy
import com.uber.autodispose.ObservableSubscribeProxy
import com.uber.autodispose.ScopeProvider
import com.uber.autodispose.SingleSubscribeProxy
import com.winsonchiu.aria.util.arch.LoggingLifecycleObserver
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single

abstract class ActivityLifecycleBoundComponent : LoggingLifecycleObserver, ScopeProvider {

    private var onFinalDestroyRelay = PublishRelay.create<Unit>()

    @CallSuper
    @MainThread
    open fun onFirstInitialize() {

    }

    @CallSuper
    @MainThread
    open fun onFinalDestroy() {
        onFinalDestroyRelay.accept(Unit)
    }

    override fun requestScope() = onFinalDestroyRelay.firstElement()

    fun <T> Single<T>.bindToLifecycle(): SingleSubscribeProxy<T> {
        return `as`(AutoDispose.autoDisposable(this@ActivityLifecycleBoundComponent))
    }

    fun <T> Flowable<T>.bindToLifecycle(): FlowableSubscribeProxy<T> {
        return `as`(AutoDispose.autoDisposable(this@ActivityLifecycleBoundComponent))
    }

    fun <T> Observable<T>.bindToLifecycle(): ObservableSubscribeProxy<T> {
        return `as`(AutoDispose.autoDisposable(this@ActivityLifecycleBoundComponent))
    }
}