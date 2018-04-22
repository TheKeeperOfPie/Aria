package com.winsonchiu.aria.util

import io.reactivex.Observable
import java.util.Optional

fun <T> Observable<Optional<T>>.arePresent() = filter { it.isPresent }.map { it.get() }