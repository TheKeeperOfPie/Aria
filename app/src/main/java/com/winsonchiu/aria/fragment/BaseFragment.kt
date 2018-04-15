package com.winsonchiu.aria.fragment

import android.content.Context
import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.ButterKnife
import butterknife.Unbinder
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.ObservableSubscribeProxy
import com.uber.autodispose.SingleSubscribeProxy
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.winsonchiu.aria.MainActivity
import com.winsonchiu.aria.dagger.ActivityComponent
import com.winsonchiu.aria.dagger.fragment.FragmentLifecycleBoundComponent
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject
import kotlin.reflect.KProperty

abstract class BaseFragment<DaggerComponent> : Fragment(),
    InjectableFragment<DaggerComponent>,
    FragmentManager.OnBackStackChangedListener {
    companion object {
        val TAG = BaseFragment::class.java.canonicalName
    }

    @get:LayoutRes
    abstract val layoutId: Int

    @Inject
    lateinit var lifecycleBoundComponents: Set<@JvmSuppressWildcards FragmentLifecycleBoundComponent>

    private lateinit var unbinder: Unbinder

    private val viewScopedVariables by lazy { ArrayList<ViewScoped<*>>() }

    lateinit var loader: FragmentLoader<DaggerComponent>

    override fun onBackStackChanged() {
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val loaderCallback = FragmentLoaderCallback<DaggerComponent>(context)
        loader = loaderManager.initLoader(0, null, loaderCallback) as FragmentLoader<DaggerComponent>

        if (loader.fragmentComponent == null) {
            val activityComponent = context.getSystemService(MainActivity.ACTIVITY_COMPONENT) as ActivityComponent
            loader.fragmentComponent = makeComponent(activityComponent)
        }

        injectSelf(loader.fragmentComponent!!)

        lifecycleBoundComponents.forEach {
            lifecycle.addObserver(it)
            it.initialize(this)
        }

        fragmentManager?.addOnBackStackChangedListener(this)
    }

    final override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflater.inflate(layoutId, container, false)

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewScopedVariables.forEach { it.onCreate() }
        unbinder = ButterKnife.bind(this, view)
    }

    @CallSuper
    override fun onDestroyView() {
        viewScopedVariables.forEach { it.onDestroy() }
        unbinder.unbind()
        super.onDestroyView()
    }

    fun <T> Single<T>.bindToLifecycle(): SingleSubscribeProxy<T> {
        return `as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this@BaseFragment)))
    }

    fun <T> Observable<T>.bindToLifecycle(): ObservableSubscribeProxy<T> {
        return `as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this@BaseFragment)))
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

        fun onCreate() {
            value = initialValue()
        }

        fun onDestroy() {
            value = null
        }
    }
}