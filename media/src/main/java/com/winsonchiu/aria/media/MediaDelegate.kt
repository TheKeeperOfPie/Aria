package com.winsonchiu.aria.media

import android.app.PendingIntent
import android.content.Context

interface MediaDelegate {

    fun notificationContentIntent(context: Context): PendingIntent
}