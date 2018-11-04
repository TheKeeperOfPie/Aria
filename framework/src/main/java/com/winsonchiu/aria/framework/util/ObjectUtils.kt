package com.winsonchiu.aria.framework.util

import java.util.Optional

fun <T> Optional<T>.orNull() = if (isPresent) get() else null