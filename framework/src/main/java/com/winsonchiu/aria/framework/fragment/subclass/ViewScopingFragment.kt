package com.winsonchiu.aria.framework.fragment.subclass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import butterknife.ButterKnife
import butterknife.Unbinder
import kotlin.reflect.KProperty

abstract class ViewScopingFragment<in ParentComponent, ChildComponent> :
    LifecycleBoundFragment<ParentComponent, ChildComponent>() {

    @get:LayoutRes
    protected abstract val layoutId: Int

    private lateinit var unbinder: Unbinder

    private val viewScopedVariables by lazy { ArrayList<ViewScoped<*>>() }

    final override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflater.inflate(layoutId, container, false)

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        unbinder = ButterKnife.bind(this, view)
        viewScopedVariables.forEach { it.onViewCreated() }
    }

    @CallSuper
    override fun onDestroyView() {
        unbinder.unbind()
        super.onDestroyView()
        viewScopedVariables.forEach { it.onViewDestroyed() }
    }

    inner class ViewScoped<Type>(
            private var initialValue: () -> Type? = { null }
    ) {
        private var value: Type? = null

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

        fun onViewCreated() {
            value = initialValue()
        }

        fun onViewDestroyed() {
            value = null
        }
    }
}