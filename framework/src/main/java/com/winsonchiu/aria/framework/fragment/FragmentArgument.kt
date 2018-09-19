package com.winsonchiu.aria.framework.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.winsonchiu.aria.framework.util.putArgument
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class FragmentArgument<DataType>(
        val key: String
) : ReadWriteProperty<Fragment, DataType> {

    var cached: DataType? = null

    @Suppress("UNCHECKED_CAST")
    override fun getValue(
            thisRef: Fragment,
            property: KProperty<*>
    ): DataType {
        if (cached != null) {
            return cached!!
        }

        cached = thisRef.arguments!!.get(key) as DataType?
        return cached!!
    }

    override fun setValue(
            thisRef: Fragment,
            property: KProperty<*>,
            value: DataType
    ) {
        thisRef.arguments = (thisRef.arguments ?: Bundle()).apply {
            putArgument(key, value)
        }
    }
}