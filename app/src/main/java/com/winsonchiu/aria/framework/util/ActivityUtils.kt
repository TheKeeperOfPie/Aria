package com.winsonchiu.aria.framework.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

fun Context.unwrapActivity(): Activity? {
    var currentContext = this

    while (currentContext !is Activity && currentContext is ContextWrapper) {
        currentContext = currentContext.baseContext
    }

    return currentContext as? Activity
}