package com.winsonchiu.aria.fragment

import android.content.Context
import android.support.v4.content.Loader

class FragmentLoader<FragmentComponent>(context: Context) : Loader<FragmentComponent>(context) {

    var memoryObject: Any? = null

    var fragmentComponent: FragmentComponent? = null
}