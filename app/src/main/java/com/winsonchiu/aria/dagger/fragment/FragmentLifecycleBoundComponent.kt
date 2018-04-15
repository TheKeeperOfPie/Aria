package com.winsonchiu.aria.dagger.fragment

import android.os.Bundle
import android.support.annotation.CallSuper
import com.winsonchiu.aria.fragment.FragmentInitializer
import com.winsonchiu.aria.util.LoggingLifecycleObserver
import kotlin.reflect.KProperty

abstract class FragmentLifecycleBoundComponent : LoggingLifecycleObserver {

    private val args = mutableListOf<ArgumentDelegate<*>>()

    private var initialized = false

    @CallSuper
    fun initialize(arguments: Bundle) {
        args.forEach { it.initialize(arguments) }

        if (!initialized) {
            initialized = true
            onFirstInitialize()
        }
    }

    protected open fun onFirstInitialize() {

    }

    fun <T : Any?> arg(arg: FragmentInitializer<*>.Arg<T?>) = ArgumentDelegate(arg).also {
        args.add(it)
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


