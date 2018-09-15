package com.winsonchiu.aria.framework.util

import android.content.Context
import com.winsonchiu.aria.framework.activity.DaggerComponentActivity
import com.winsonchiu.aria.framework.application.AriaApplication
import com.winsonchiu.aria.framework.dagger.ApplicationComponent
import com.winsonchiu.aria.framework.dagger.activity.ActivityComponent

val Context.applicationComponent
    get() = (applicationContext.getSystemService(AriaApplication.APPLICATION_COMPONENT) as ApplicationComponent)

val Context.activityComponent
    get() = (getSystemService(DaggerComponentActivity.ACTIVITY_COMPONENT) as ActivityComponent)