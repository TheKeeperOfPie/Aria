package com.winsonchiu.aria.main

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.winsonchiu.aria.framework.dagger.activity.ActivityComponent
import com.winsonchiu.aria.framework.dagger.activity.ActivityLifecycleBoundComponent
import com.winsonchiu.aria.framework.dagger.activity.DaggerComponentActivity
import com.winsonchiu.aria.framework.dagger.activity.DaggerConstants
import javax.inject.Inject

class MainActivityViewModel(activity: AppCompatActivity) : ViewModel() {

    @Inject
    lateinit var lifecycleBoundComponents: Set<@JvmSuppressWildcards ActivityLifecycleBoundComponent>

    init {
        (activity.getSystemService(DaggerConstants.ACTIVITY_COMPONENT) as ActivityComponent).inject(this)
        lifecycleBoundComponents.forEach { it.onFirstInitialize() }
    }

    override fun onCleared() {
        super.onCleared()

        lifecycleBoundComponents.forEach { it.onFinalDestroy() }
    }
}