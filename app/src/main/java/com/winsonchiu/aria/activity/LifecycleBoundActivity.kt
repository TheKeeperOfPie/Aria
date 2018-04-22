package com.winsonchiu.aria.activity

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.ObservableSubscribeProxy
import com.uber.autodispose.SingleSubscribeProxy
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.winsonchiu.aria.dagger.activity.ActivityLifecycleBoundComponent
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

abstract class LifecycleBoundActivity : DaggerComponentActivity() {

    @Inject
    lateinit var lifecycleBoundComponents: Set<@JvmSuppressWildcards ActivityLifecycleBoundComponent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return MainActivityViewModel(this@LifecycleBoundActivity) as T
            }
        }).get(MainActivityViewModel::class.java)

        lifecycleBoundComponents.forEach {
            lifecycle.addObserver(it)
        }
    }

    fun <T> Single<T>.bindToLifecycle(): SingleSubscribeProxy<T> {
        return `as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this@LifecycleBoundActivity)))
    }

    fun <T> Observable<T>.bindToLifecycle(): ObservableSubscribeProxy<T> {
        return `as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this@LifecycleBoundActivity)))
    }
}