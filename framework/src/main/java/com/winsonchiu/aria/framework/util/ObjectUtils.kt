package com.winsonchiu.aria.framework.util

import java.util.Optional

fun <T> Optional<T>.orNull() = if (isPresent) get() else null

inline fun <T> compareOrNull(a: T, b: T, selector: (T) -> Comparable<*>?): Int? {
    val first = selector.invoke(a)
    val second = selector.invoke(b)

    if (first == second) return null
    if (first == null) return -1
    if (second == null) return 1

    @Suppress("UNCHECKED_CAST")
    return (first as Comparable<Any>).compareTo(second)
}