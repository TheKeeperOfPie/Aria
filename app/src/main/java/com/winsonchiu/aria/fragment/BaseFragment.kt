package com.winsonchiu.aria.fragment

import android.content.Context
import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.ButterKnife
import butterknife.Unbinder
import com.winsonchiu.aria.MainActivity
import com.winsonchiu.aria.dagger.ActivityComponent
import kotlin.reflect.KProperty

abstract class BaseFragment<DaggerComponent> : Fragment(), InjectableFragment<DaggerComponent> {

    @get:LayoutRes abstract val layoutId: Int

    private lateinit var unbinder: Unbinder

    private val viewScopedVariables by lazy { ArrayList<ViewScoped<*>>() }

    private lateinit var loader: FragmentLoader<DaggerComponent>

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val loaderCallback = FragmentLoaderCallback<DaggerComponent>(context)
        loader = loaderManager.initLoader(0, null, loaderCallback) as FragmentLoader<DaggerComponent>

        if (loader.fragmentComponent == null) {
            val activityComponent = context.getSystemService(MainActivity.ACTIVITY_COMPONENT) as ActivityComponent
            loader.fragmentComponent = makeComponent(activityComponent)
        }
    }

    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layoutId, container, false)
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        unbinder = ButterKnife.bind(this, view)
    }

    @CallSuper
    override fun onDestroyView() {
        viewScopedVariables.forEach { it.clear() }
        unbinder.unbind()
        super.onDestroyView()
    }

    inner class ViewScoped<Type>(
            private var value: Type? = null
    ) {
        init {
            viewScopedVariables += this
        }

        @Suppress("UNCHECKED_CAST")
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Type {
            return value as Type
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Type) {
            this.value = value
        }

        fun clear() {
            value = null
        }
    }
}