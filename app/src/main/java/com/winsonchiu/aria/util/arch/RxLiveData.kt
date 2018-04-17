package com.winsonchiu.aria.util.arch

import io.reactivex.functions.Consumer

class RxLiveData<ValueType> : DefaultLiveData<ValueType>, Consumer<ValueType> {

    constructor() : super()
    constructor(defaultValue: ValueType) : super(defaultValue)

    override fun accept(value: ValueType) {
        postValue(value)
    }
}