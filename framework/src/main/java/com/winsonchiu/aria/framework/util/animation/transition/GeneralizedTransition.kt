package com.winsonchiu.aria.framework.util.animation.transition

import android.animation.Animator
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import java.util.concurrent.atomic.AtomicReference

typealias TransitionValuesFramework = android.transition.TransitionValues
typealias TransitionValuesSupport = androidx.transition.TransitionValues

typealias TransitionListenerFramework = android.transition.Transition.TransitionListener
typealias TransitionListenerSupport = androidx.transition.Transition.TransitionListener

/**
 * This is a generalized wrapper of [android.transition.Transition] and
 * [androidx.transition.Transition].
 *
 * It is designed to be one-way, meaning you set both enter and return or exit and reenter, with
 * reversed values. It will not automatically reverse an animation given opposite start and end
 * values. This is done to simplify the appearance/disappearance logic because there is no
 * easy way to generalize which direction a transition is occurring in.
 *
 * In order for an Activity/Fragment transition to work properly, the system must recognize the view
 * you're trying targeting as a valid transition view. Otherwise it will remove it from the eligible
 * view list and not call into your Transition subclass.
 *
 * For a view to be valid, it has to pass the following checks:
 * - All parents must be transitionGroup == false (see below)
 * - Visibility must be View.VISIBLE; INVISIBLE will not work
 * - Layout at least 1x1 pixels
 * - Not fully clipped by overlapping views or its parent
 *
 * In addition, if the view is a ViewGroup:
 * - Must be transitionGroup == true
 *
 * If you find that your transitions is not called for a specific target, you must check that
 * all of its parents are not transition groups, read through [ViewGroup.isTransitionGroup].
 *
 * You may explicitly set [ViewGroup.setTransitionGroup] to false, but if you do not, it must have:
 * - No background
 * - No transition name
 * - No non-default outline provider
 *
 * If after verifying the above, and trying all the overlay modes available to [GhostViewOverlay],
 * a Transition still isn't working, the best strategy is to open the debugger and set breakpoints
 * inside [android.app.EnterTransitionCoordinator] or [android.app.ExitTransitionCoordinator].
 * Specifically around [android.app.ActivityTransitionCoordinator.mTransitioningViews] and
 * [android.transition.Transition.isValidTarget].
 */
abstract class GeneralizedTransition(
        private val logTag: String? = null
) {

    fun captureStart(view: View, values: MutableMap<String, Any?>) {
        onCaptureStart(view, values)
        logTag?.let { Log.d(it, "captureStart() called with view = $view, values = $values") }
    }

    fun captureEnd(view: View, values: MutableMap<String, Any?>) {
        onCaptureEnd(view, values)
        logTag?.let { Log.d(it, "captureEnd() called with view = $view, values = $values") }
    }

    fun createAnimator(
            sceneRoot: ViewGroup,
            startView: View?,
            endView: View?,
            startValues: MutableMap<String, Any?>?,
            endValues: MutableMap<String, Any?>?
    ): Animator? {
        logTag?.let { Log.d(it, "createAnimator() called with startView = $startView, endView = $endView, startValues = $startValues, endValues = $endValues") }
        return onCreateAnimator(sceneRoot, startView, endView, startValues, endValues)
    }

    /**
     * Capture values must fail a valuesBeforeCapture.equals(valuesAfterCapture) check
     * in order for a transition to be run. Generally you should set all the constructor
     * parameters from a subclass as values.
     */
    protected abstract fun onCaptureStart(view: View, values: MutableMap<String, Any?>)

    /**
     * Capture values must fail a valuesBeforeCapture.equals(valuesAfterCapture) check
     * in order for a transition to be run. Generally you should set all the constructor
     * parameters from a subclass as values.
     */
    protected abstract fun onCaptureEnd(view: View, values: MutableMap<String, Any?>)

    protected abstract fun onCreateAnimator(
            sceneRoot: ViewGroup,
            startView: View?,
            endView: View?,
            startValues: MutableMap<String, Any?>?,
            endValues: MutableMap<String, Any?>?
    ): Animator?

    /**
     * Called right before an animator object is requested. Mostly used by parent classes
     * to set something on the view as soon as possible.
     */
    open fun onBeforeCreateAnimator(
            sceneRoot: ViewGroup,
            startView: View?,
            endView: View?,
            startValues: MutableMap<String, Any?>?,
            endValues: MutableMap<String, Any?>?
    ) {
    }

    /**
     * For acting on events on the root Transition rather than the specific animator.
     */
    @CallSuper
    open fun getListener(
            sceneRoot: ViewGroup,
            startView: View?,
            endView: View?,
            startValues: MutableMap<String, Any?>?,
            endValues: MutableMap<String, Any?>?,
            removeFunction: () -> Unit
    ): GeneralizedTransitionListener? = null

    open fun forFramework(): android.transition.Transition = TransitionFramework(this)

    open fun forSupport(): androidx.transition.Transition = TransitionSupport(this)
}

/**
 * [GeneralizedTransition] wrapper which delegates to a framework [android.transition.Transition]
 */
private class TransitionFramework(
        private val generalizedTransition: GeneralizedTransition
) : android.transition.Transition() {
    override fun captureStartValues(transitionValues: TransitionValuesFramework) {
        generalizedTransition.captureStart(transitionValues.view, transitionValues.values)
    }

    override fun captureEndValues(transitionValues: TransitionValuesFramework) {
        generalizedTransition.captureEnd(transitionValues.view, transitionValues.values)
    }

    override fun createAnimator(
            sceneRoot: ViewGroup,
            startValues: TransitionValuesFramework?,
            endValues: TransitionValuesFramework?
    ): Animator? {
        generalizedTransition.onBeforeCreateAnimator(sceneRoot, startValues?.view, endValues?.view, startValues?.values, endValues?.values)

        val animator = generalizedTransition.createAnimator(sceneRoot, startValues?.view, endValues?.view, startValues?.values, endValues?.values)
        val listenerReference = AtomicReference<TransitionListenerFramework>()
        val function: () -> Unit = {
            removeListener(listenerReference.get())
        }

        generalizedTransition.getListener(sceneRoot, startValues?.view, endValues?.view, startValues?.values, endValues?.values, function)?.let {
            listenerReference.set(it.forFramework())
            addListener(listenerReference.get())
        }

        return animator
    }
}

/**
 * [GeneralizedTransition] wrapper which delegates to [androidx.transition.Transition]
 */
private class TransitionSupport(
        private val generalizedTransition: GeneralizedTransition
) : androidx.transition.Transition() {
    override fun captureStartValues(transitionValues: TransitionValuesSupport) {
        generalizedTransition.captureStart(transitionValues.view, transitionValues.values)
    }

    override fun captureEndValues(transitionValues: TransitionValuesSupport) {
        generalizedTransition.captureEnd(transitionValues.view, transitionValues.values)
    }

    override fun createAnimator(
            sceneRoot: ViewGroup,
            startValues: TransitionValuesSupport?,
            endValues: TransitionValuesSupport?
    ): Animator? {
        generalizedTransition.onBeforeCreateAnimator(sceneRoot, startValues?.view, endValues?.view, startValues?.values, endValues?.values)

        val animator = generalizedTransition.createAnimator(sceneRoot, startValues?.view, endValues?.view, startValues?.values, endValues?.values)
        val listenerReference = AtomicReference<TransitionListenerSupport>()
        val function: () -> Unit = {
            removeListener(listenerReference.get())
        }

        generalizedTransition.getListener(sceneRoot, startValues?.view, endValues?.view, startValues?.values, endValues?.values, function)?.let {
            listenerReference.set(it.forSupport())
            addListener(listenerReference.get())
        }

        return animator
    }
}

/**
 * Generalized extension of [android.transition.Transition.TransitionListener]
 * and [androidx.transition.Transition.TransitionListener]
 */
abstract class GeneralizedTransitionListener(
        private val innerListener: GeneralizedTransitionListener? = null
) {

    open fun onTransitionStart() {}

    open fun onTransitionEnd() {}

    open fun onTransitionCancel() {}

    open fun onTransitionPause() {}

    open fun onTransitionResume() {}

    fun forFramework(): TransitionListenerFramework {
        return object : TransitionListenerFramework {
            override fun onTransitionEnd(transition: android.transition.Transition?) {
                this@GeneralizedTransitionListener.onTransitionEnd()
                innerListener?.onTransitionEnd()
            }

            override fun onTransitionResume(transition: android.transition.Transition?) {
                this@GeneralizedTransitionListener.onTransitionResume()
                innerListener?.onTransitionResume()
            }

            override fun onTransitionPause(transition: android.transition.Transition?) {
                this@GeneralizedTransitionListener.onTransitionPause()
                innerListener?.onTransitionPause()
            }

            override fun onTransitionCancel(transition: android.transition.Transition?) {
                this@GeneralizedTransitionListener.onTransitionCancel()
                innerListener?.onTransitionCancel()
            }

            override fun onTransitionStart(transition: android.transition.Transition?) {
                this@GeneralizedTransitionListener.onTransitionStart()
                innerListener?.onTransitionStart()
            }
        }
    }

    fun forSupport(): androidx.transition.Transition.TransitionListener {
        return object : androidx.transition.Transition.TransitionListener {
            override fun onTransitionEnd(transition: androidx.transition.Transition) {
                this@GeneralizedTransitionListener.onTransitionEnd()
                innerListener?.onTransitionEnd()
            }

            override fun onTransitionResume(transition: androidx.transition.Transition) {
                this@GeneralizedTransitionListener.onTransitionResume()
                innerListener?.onTransitionResume()
            }

            override fun onTransitionPause(transition: androidx.transition.Transition) {
                this@GeneralizedTransitionListener.onTransitionPause()
                innerListener?.onTransitionPause()
            }

            override fun onTransitionCancel(transition: androidx.transition.Transition) {
                this@GeneralizedTransitionListener.onTransitionCancel()
                innerListener?.onTransitionCancel()
            }

            override fun onTransitionStart(transition: androidx.transition.Transition) {
                this@GeneralizedTransitionListener.onTransitionStart()
                innerListener?.onTransitionStart()
            }
        }
    }
}
