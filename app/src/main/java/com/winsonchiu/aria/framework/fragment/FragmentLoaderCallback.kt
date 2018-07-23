package com.winsonchiu.aria.framework.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader

class FragmentLoaderCallback<Component>(private val context: Context) : LoaderManager.LoaderCallbacks<Component> {

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Component> {
        return FragmentLoader(context)
    }

    override fun onLoadFinished(loader: Loader<Component>, data: Component) {

    }

    override fun onLoaderReset(loader: Loader<Component>) {

    }
}