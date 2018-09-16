package com.winsonchiu.aria.framework.util

@Suppress("ConstantConditionIf")
object Failsafe {

    private const val PRINT_TRACE = true

    private const val FAIL = false

    fun <T> orNull(block: () -> T) = try {
        block()
    } catch (e: Exception) {
        if (PRINT_TRACE) {
            e.printStackTrace()
        }
        if (FAIL) {
            throw e
        }
        null
    }

    fun <T> withDefault(default: T, block: () -> T) = try {
        block()
    } catch (e: Exception) {
        if (PRINT_TRACE) {
            e.printStackTrace()
        }
        if (FAIL) {
            throw e
        }
        default
    }

    fun <T> withDefault(default: () -> T, block: () -> T) = try {
        block()
    } catch (e: Exception) {
        if (PRINT_TRACE) {
            e.printStackTrace()
        }
        if (FAIL) {
            throw e
        }
        default
    }
}