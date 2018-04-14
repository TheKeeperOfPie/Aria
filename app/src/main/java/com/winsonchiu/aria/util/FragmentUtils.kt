package com.winsonchiu.aria.util

import android.support.annotation.IdRes
import android.support.v4.app.FragmentManager

fun FragmentManager.hasFragment(@IdRes containerId: Int): Boolean {
    return findFragmentById(containerId) != null
}