package com.winsonchiu.aria.fragment.subclass

import android.content.Context
import android.support.v4.app.Fragment
import com.winsonchiu.aria.activity.DaggerComponentActivity
import com.winsonchiu.aria.fragment.FragmentLoader
import com.winsonchiu.aria.fragment.FragmentLoaderCallback

abstract class InjectingFragment<in ParentComponent, ChildComponent> : Fragment() {

    private lateinit var loader: FragmentLoader<ChildComponent>

    override fun onAttach(context: Context) {
        super.onAttach(context)

        injectDependencies(context)
    }

    @Suppress("UNCHECKED_CAST")
    private fun injectDependencies(context: Context) {
        loader = getFragmentLoader()

        if (loader.fragmentComponent == null) {
            val parentComponent = (parentFragment as? InjectingFragment<*, *>)?.loader?.fragmentComponent
                    ?: context.getSystemService(DaggerComponentActivity.ACTIVITY_COMPONENT)

            loader.fragmentComponent = makeComponent(parentComponent as ParentComponent)
        }

        injectSelf(loader.fragmentComponent!!)
    }

    private fun getFragmentLoader(): FragmentLoader<ChildComponent> {
        return loaderManager.initLoader(
                0,
                null,
                FragmentLoaderCallback<ChildComponent>(context!!)
        ) as FragmentLoader<ChildComponent>
    }

    abstract fun makeComponent(parentComponent: ParentComponent): ChildComponent

    abstract fun injectSelf(component: ChildComponent)
}