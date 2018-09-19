package com.winsonchiu.aria.framework.util

import androidx.annotation.NonNull
import androidx.collection.LruCache

operator fun <K, V> LruCache<K, V>.set(key: K, @NonNull value: V) = put(key, value)