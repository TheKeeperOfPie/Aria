package com.winsonchiu.aria.util

object Failsafe {

    private const val PRINT_TRACE = false

    fun <T> orNull(block: () -> T) = try {
        block()
    } catch (e: Exception) {
        if (PRINT_TRACE) {
            e.printStackTrace()
        }
        null
    }

    fun <T> withDefault(default: T, block: () -> T) = try {
        block()
    } catch (e: Exception) {
        if (PRINT_TRACE) {
            e.printStackTrace()
        }
        default
    }

    fun <T> withDefault(default: () -> T, block: () -> T) = try {
        block()
    } catch (e: Exception) {
        if (PRINT_TRACE) {
            e.printStackTrace()
        }
        default
    }
}