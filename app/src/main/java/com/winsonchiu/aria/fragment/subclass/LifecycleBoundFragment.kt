package com.winsonchiu.aria.fragment.subclass

import android.content.Context
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.ObservableSubscribeProxy
import com.uber.autodispose.SingleSubscribeProxy
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.winsonchiu.aria.dagger.fragment.FragmentLifecycleBoundComponent
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

abstract class LifecycleBoundFragment<in ParentComponent, ChildComponent> :
    InjectingFragment<ParentComponent, ChildComponent>() {

    @Inject
    lateinit var lifecycleBoundComponents: Set<@JvmSuppressWildcards FragmentLifecycleBoundComponent>

    override fun onAttach(context: Context) {
        super.onAttach(context)

        lifecycleBoundComponents.forEach {
            it.initialize(this)
            lifecycle.addObserver(it)
        }
    }

    fun <T> Single<T>.bindToLifecycle(): SingleSubscribeProxy<T> {
        return `as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this@LifecycleBoundFragment)))
    }

    fun <T> Observable<T>.bindToLifecycle(): ObservableSubscribeProxy<T> {
        return `as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this@LifecycleBoundFragment)))
    }
}