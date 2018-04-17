package com.winsonchiu.aria.dagger.fragment

import android.arch.lifecycle.LifecycleOwner
import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import com.jakewharton.rxrelay2.PublishRelay
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.FlowableSubscribeProxy
import com.uber.autodispose.ObservableSubscribeProxy
import com.uber.autodispose.ScopeProvider
import com.uber.autodispose.SingleSubscribeProxy
import com.winsonchiu.aria.fragment.FragmentInitializer
import com.winsonchiu.aria.util.arch.LoggingLifecycleObserver
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

abstract class FragmentLifecycleBoundComponent : LoggingLifecycleObserver, FragmentManager.OnBackStackChangedListener, ScopeProvider {

    private val args = mutableListOf<ArgumentDelegate<*>>()

    private var initialized = false

    private var isInBackStack = false

    private var fragmentReference: WeakReference<Fragment>? = null

    private var onFinalDestroyRelay = PublishRelay.create<Unit>()

    @CallSuper
    fun initialize(fragment: Fragment) {
        val arguments = fragment.arguments
        if (arguments != null) {
            args.forEach { it.initialize(arguments) }
        }

        if (!initialized) {
            initialized = true
            onFirstInitialize(fragment)
        }
    }

    @CallSuper
    protected open fun onFirstInitialize(fragment: Fragment) {

    }

    @CallSuper
    protected open fun onFinalDestroy(fragment: Fragment) {
        onFinalDestroyRelay.accept(Unit)
    }

    override fun requestScope() = onFinalDestroyRelay.firstElement()

    override fun onBackStackChanged() {
        // TODO: See if ViewModel.onCleared is a better solution
        fragmentReference?.get()?.let {
            val backStackNesting = Fragment::class.java.getDeclaredField("mBackStackNesting")
                    .apply { isAccessible = true }
                    .getInt(it)

            isInBackStack = backStackNesting > 0
        }
    }

    @CallSuper
    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        (owner as? Fragment)?.let {
            fragmentReference = WeakReference(it)
            it.fragmentManager?.addOnBackStackChangedListener(this)
        }
    }

    @CallSuper
    override fun onDestroy(owner: LifecycleOwner) {
        fragmentReference?.clear()

        (owner as? Fragment)?.let {
            // If Activity is being destroyed for good or explicitly popped from the back stack, tear down
            if (it.activity?.isFinishing == true || !isInBackStack) {
                onFinalDestroy(it)
            }

            it.fragmentManager?.removeOnBackStackChangedListener(this)
        }

        super.onDestroy(owner)
    }

    fun <T : Any?> arg(arg: FragmentInitializer<*>.Arg<T?>) = ArgumentDelegate(arg).also {
        args.add(it)
    }

    fun <T> Single<T>.bindToLifecycle(): SingleSubscribeProxy<T> {
        return `as`(AutoDispose.autoDisposable(this@FragmentLifecycleBoundComponent))
    }

    fun <T> Flowable<T>.bindToLifecycle(): FlowableSubscribeProxy<T> {
        return `as`(AutoDispose.autoDisposable(this@FragmentLifecycleBoundComponent))
    }

    fun <T> Observable<T>.bindToLifecycle(): ObservableSubscribeProxy<T> {
        return `as`(AutoDispose.autoDisposable(this@FragmentLifecycleBoundComponent))
    }

    class ArgumentDelegate<Type>(
            private val arg: FragmentInitializer<*>.Arg<Type>
    ) {

        var value: Type? = null

        @Suppress("UNCHECKED_CAST")
        fun initialize(arguments: Bundle) {
            value = arguments.get(arg.key) as Type
        }

        @Suppress("UNCHECKED_CAST")
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Type {
            return value as Type
        }
    }
}


