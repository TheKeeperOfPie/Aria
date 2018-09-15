package com.winsonchiu.aria.framework.util

import io.reactivex.Maybe
import io.reactivex.Observable
import java.util.Optional

fun <T> Observable<Optional<T>>.arePresent() = filter { it.isPresent }.map { it.get() }

fun <T, R> Observable<T?>.mapNonNull(function: (T) -> R?): Observable<R> = flatMapMaybe {
    Maybe.fromCallable { function(it) }
}