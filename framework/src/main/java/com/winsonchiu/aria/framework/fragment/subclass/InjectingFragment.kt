package com.winsonchiu.aria.framework.fragment.subclass

import android.content.Context
import androidx.fragment.app.Fragment
import com.winsonchiu.aria.framework.dagger.activity.DaggerConstants
import com.winsonchiu.aria.framework.fragment.FragmentLoader
import com.winsonchiu.aria.framework.fragment.FragmentLoaderCallback

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
                    ?: context.getSystemService(DaggerConstants.ACTIVITY_COMPONENT)

            loader.fragmentComponent = makeComponent(parentComponent as ParentComponent)
        }

        injectSelf(loader.fragmentComponent!!)
    }

    // TODO: Move to ViewModel
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