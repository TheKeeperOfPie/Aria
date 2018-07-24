package com.winsonchiu.aria.framework.util

import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
import android.os.PersistableBundle
import android.support.annotation.IdRes
import android.support.v4.app.FragmentManager
import android.util.Size
import android.util.SizeF

fun FragmentManager.hasFragment(@IdRes containerId: Int): Boolean {
    return findFragmentById(containerId) != null
}

fun Bundle.putArgument(key: String, value: Any?) {
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
        is ArrayList<*> -> putParcelableArrayList(key, value as ArrayList<out Parcelable>)
        else -> throw IllegalStateException("Invalid bundle value")
    }
}