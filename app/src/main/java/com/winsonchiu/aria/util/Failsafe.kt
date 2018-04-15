package com.winsonchiu.aria.util

import com.winsonchiu.aria.BuildConfig

object Failsafe {

    fun <T> withDefault(default: T, block: () -> T) = try {
        block()
    } catch (e: Exception) {
        if (BuildConfig.DEBUG) {
            e.printStackTrace()
        }
        default
    }

    fun <T> withDefault(default: () -> T, block: () -> T) = try {
        block()
    } catch (e: Exception) {
        if (BuildConfig.DEBUG) {
            e.printStackTrace()
        }
        default
    }
}