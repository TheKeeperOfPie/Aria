package com.winsonchiu.aria.framework.dagger

import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.winsonchiu.aria.main.MainActivity
import com.winsonchiu.aria.media.MediaDelegate
import dagger.Module
import dagger.Provides

@Module
class ApplicationModule(
        private val application: Application
) {

    @Provides
    fun provideApplication() = application

    @Provides
    fun provideMediaDelegate() = object : MediaDelegate {
        override fun notificationContentIntent(context: Context): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

            return PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        }
    }
}