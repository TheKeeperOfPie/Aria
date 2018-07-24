package com.winsonchiu.aria.framework.menu.itemsheet

import android.os.Parcelable
import kotlinx.android.parcel.RawValue
import kotlin.reflect.KClass

interface ItemsMenuItem : Parcelable {

    val data: Parcelable
    val clazz: @RawValue KClass<*>
}

interface ItemsMenuView<DataType> {

    fun bindData(data: DataType)
}