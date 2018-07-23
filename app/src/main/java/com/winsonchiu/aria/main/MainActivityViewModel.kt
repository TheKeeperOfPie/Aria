package com.winsonchiu.aria.main

import android.arch.lifecycle.ViewModel
import android.support.v7.app.AppCompatActivity
import com.winsonchiu.aria.framework.activity.DaggerComponentActivity
import com.winsonchiu.aria.framework.dagger.activity.ActivityComponent
import com.winsonchiu.aria.framework.dagger.activity.ActivityLifecycleBoundComponent
import javax.inject.Inject

class MainActivityViewModel(activity: AppCompatActivity) : ViewModel() {

    @Inject
    lateinit var lifecycleBoundComponents: Set<@JvmSuppressWildcards ActivityLifecycleBoundComponent>

    init {
        (activity.getSystemService(DaggerComponentActivity.ACTIVITY_COMPONENT) as ActivityComponent).inject(this)
        lifecycleBoundComponents.forEach { it.onFirstInitialize() }
    }

    override fun onCleared() {
        super.onCleared()

        lifecycleBoundComponents.forEach { it.onFinalDestroy() }
    }
}