package com.winsonchiu.aria.framework.util

import io.reactivex.Observable
import java.util.*

fun <T> Observable<Optional<T>>.arePresent() = filter { it.isPresent }.map { it.get() }