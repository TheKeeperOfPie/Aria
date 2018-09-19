package com.winsonchiu.aria.framework.application

import android.annotation.SuppressLint
import android.app.Application
import android.os.StrictMode
import androidx.annotation.CallSuper
import com.squareup.leakcanary.LeakCanary
import com.winsonchiu.aria.framework.BuildConfig

@SuppressLint("Registered")
abstract class LeakCanaryApplication : Application() {

    @CallSuper
    final override fun onCreate() {
        super.onCreate()

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return
        }

        if (BuildConfig.DEBUG) {
            enableStrictMode()
        }

        LeakCanary.install(this)

        onCreateApp()
    }

    @CallSuper
    protected open fun enableStrictMode() {
        StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                        .detectAll()
                        .penaltyLog()
                        .build()
        )

        StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                        .detectAll()
                        .penaltyLog()
                        .build()
        )
    }

    protected open fun onCreateApp() {}
}