package com.winsonchiu.aria.framework.fragment

import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import com.winsonchiu.aria.framework.util.putArgument

@Suppress("UNCHECKED_CAST")
inline fun <FragmentType : Fragment, T : FragmentInitializer<FragmentType>> T.build(block: T.() -> Unit): FragmentType {
    block(this)
    return buildFragment()
}

@Suppress("UNCHECKED_CAST")
fun <FragmentType : Fragment, T : FragmentInitializer<FragmentType>> T.build(): FragmentType {
    return buildFragment()
}

inline fun <reified T : Any?> Fragment.arg(arg: FragmentInitializer<*>.Arg<T?>): Lazy<T?> {
    return lazy { arguments?.get(arg.key) as T? }
}

abstract class FragmentInitializer<out FragmentType : Fragment>(private val fragment: () -> FragmentType) {

    private val argumentsMap = mutableMapOf<String, Any?>()

    fun buildFragment() = fragment().apply {
        arguments = (arguments ?: Bundle()).putExtras()
    }

    @Suppress("unused")
    inner class Arg<T>(val key: String)

    protected fun string(key: String) = Arg<String?>(key)
    protected fun boolean(key: String) = Arg<Boolean>(key)
    protected fun long(key: String) = Arg<Long>(key)
    protected fun <ParcelableType : Parcelable> parcelable(key: String) = Arg<ParcelableType>(key)

    infix fun <ValueType> Arg<ValueType>.put(value: ValueType?) = argumentsMap.set(key, value)

    private fun Bundle?.putExtras() = this?.apply {
        for ((key, value) in argumentsMap) {
            putArgument(key, value)
        }
    }
}