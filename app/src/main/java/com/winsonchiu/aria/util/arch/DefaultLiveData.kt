package com.winsonchiu.aria.util.arch

import android.arch.lifecycle.MutableLiveData

open class DefaultLiveData<ValueType>() : MutableLiveData<ValueType>() {

    constructor(defaultValue: ValueType) : this() {
        value = defaultValue
    }
}