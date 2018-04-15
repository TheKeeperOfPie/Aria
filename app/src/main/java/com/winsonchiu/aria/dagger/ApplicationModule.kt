package com.winsonchiu.aria.dagger

import android.app.Application
import dagger.Module
import dagger.Provides

@Module
class ApplicationModule(
        private val application: Application
) {

    @Provides
    fun provideApplication() = application
}