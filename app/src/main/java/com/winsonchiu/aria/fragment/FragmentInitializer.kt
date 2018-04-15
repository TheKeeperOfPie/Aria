package com.winsonchiu.aria.fragment

import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
import android.os.PersistableBundle
import android.support.v4.app.Fragment
import android.util.Size
import android.util.SizeF

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
            when (value) {
                null -> Unit
                is IBinder -> putBinder(key, value)
                is Boolean -> putBoolean(key, value)
                is Bundle -> putBundle(key, value)
                is Byte -> putByte(key, value)
                is Char -> putChar(key, value)
                is CharSequence -> putCharSequence(key, value)
                is Double -> putDouble(key, value)
                is Float -> putFloat(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Parcelable -> putParcelable(key, value)
                is Short -> putShort(key, value)
                is Size -> putSize(key, value)
                is SizeF -> putSizeF(key, value)
                is String -> putString(key, value)
                is PersistableBundle -> putAll(value)
                else -> throw IllegalStateException("Invalid bundle value")
            }
        }
    }
}