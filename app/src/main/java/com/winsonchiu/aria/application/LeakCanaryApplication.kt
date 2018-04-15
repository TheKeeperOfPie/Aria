package com.winsonchiu.aria.application

import android.annotation.SuppressLint
import android.app.Application
import android.os.StrictMode
import android.support.annotation.CallSuper
import com.squareup.leakcanary.LeakCanary
import com.winsonchiu.aria.BuildConfig
import com.winsonchiu.aria.MainActivity

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

    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build())

        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build())

        // To fix a quirk with rotating multiple times quickly, increment the expected Activity count
        val incrementExpectedActivityCount = StrictMode::class.java.getMethod("incrementExpectedActivityCount", Class::class.java)
        incrementExpectedActivityCount.invoke(null, MainActivity::class.java)
        incrementExpectedActivityCount.invoke(null, MainActivity::class.java)
        incrementExpectedActivityCount.invoke(null, MainActivity::class.java)
    }

    protected fun onCreateApp() {}
}