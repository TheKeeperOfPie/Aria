package com.winsonchiu.aria.framework.fragment

import android.content.Context
import androidx.loader.content.Loader

// TODO: Replace with ViewModel
class FragmentLoader<FragmentComponent>(context: Context) : Loader<FragmentComponent>(context) {

    var memoryObject: Any? = null

    var fragmentComponent: FragmentComponent? = null
}