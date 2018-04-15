package com.winsonchiu.aria.util.animation.transition

import android.animation.Animator
import android.annotation.SuppressLint
import android.graphics.Matrix
import android.support.transition.ChangeTransform
import android.transition.Visibility
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroupOverlay
import com.winsonchiu.aria.util.animation.transition.GhostViewOverlay.OverlayMode.FRAMEWORK_VISIBILITY
import com.winsonchiu.aria.util.animation.transition.GhostViewOverlay.OverlayMode.GHOST
import com.winsonchiu.aria.util.animation.transition.GhostViewOverlay.OverlayMode.NONE
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * This is a copied implementation of [ChangeTransform]'s GhostView overlay code which allows
 * an entrance animation to use an overlay so it's always above the previous screen.
 */
abstract class GhostViewOverlay(
        private val overlayMode: OverlayMode = NONE
) : GeneralizedTransition() {

    enum class OverlayMode {
        /**
         * Do not modify this view. Also means it will not be pulled out of its current hierarchy.
         */
        NONE,

        /**
         * Use a system GhostView, which draws in an overlay without re-parenting.
         *
         * It performs some system tricky to allows us to temporarily move the view to an overlay
         * without affect the view hierarchy.
         *
         * This is mostly used for Fragment to Fragment enter transitions where you need
         * the incoming Fragment to overlay the exiting Fragment.
         *
         * This is used instead of [FRAMEWORK_VISIBILITY]'s normal [ViewGroupOverlay]
         * because that will not allow us to return the view hierarchy to normal after
         * the transition is finished. [FRAMEWORK_VISIBILITY] assumes the view will be
         * destroyed after, which is not the case with an entering Fragment.
         */
        GHOST,

        /**
         * Delegate to the framework [Visibility] transition. The relevant behavior is that
         * during an exit or return animation, this will allow us to place all the views
         * above the rest of the hierarchy.
         *
         * This is also required to properly animate a full resolution view without using a
         * simplified Bitmap, as the system does by default.
         *
         * This variant assumes the view hierarchy will be destroyed after, as it does not
         * retain parent child relationships, so this should only be used for animations where
         * the animating object is removed.
         *
         * Except for one case. During a Fragment exit transition, if you wish to keep a view
         * stationary, without animating at all, you will need this variant so that the system
         * keeps the view alive and onscreen. This is implemented in [DoNothingAsOverlay].
         */
        FRAMEWORK_VISIBILITY
    }

    override fun getListener(sceneRoot: ViewGroup, startView: View?, endView: View?, startValues: MutableMap<String, Any?>?, endValues: MutableMap<String, Any?>?, removeFunction: () -> Unit): GeneralizedTransitionListener? {
        val parentListener = super.getListener(sceneRoot, startView, endView, startValues, endValues, removeFunction)
        if (overlayMode != GHOST) {
            return parentListener
        }

        val view = startView ?: endView ?: return parentListener
        val ghostView = GhostViewImpl.addGhost(view, sceneRoot) ?: return parentListener

        return object : GeneralizedTransitionListener(parentListener) {
            override fun onTransitionEnd() {
                removeFunction()
                GhostViewImpl.removeGhost(view)
            }

            override fun onTransitionPause() {
                ghostView.setVisibility(View.INVISIBLE)
            }

            override fun onTransitionResume() {
                ghostView.setVisibility(View.VISIBLE)
            }
        }
    }

    override fun forFramework(): android.transition.Transition {
        return when (overlayMode) {
            FRAMEWORK_VISIBILITY -> VisibilityFramework(this)
            else -> super.forFramework()
        }
    }

    override fun forSupport(): android.support.transition.Transition {
        return when (overlayMode) {
            FRAMEWORK_VISIBILITY -> VisibilitySupport(this)
            else -> super.forSupport()
        }
    }

    /**
     * As opposed to [TransitionFramework], this delegates to a [android.transition.Visibility],
     * which has its own logic to shift views to a [ViewGroupOverlay]
     */
    private class VisibilityFramework(
            private val generalizedTransition: GeneralizedTransition
    ) : android.transition.Visibility() {

        override fun captureStartValues(transitionValues: TransitionValuesFramework) {
            super.captureStartValues(transitionValues)
            generalizedTransition.captureStart(transitionValues.view, transitionValues.values)
        }

        override fun captureEndValues(transitionValues: TransitionValuesFramework) {
            super.captureEndValues(transitionValues)
            generalizedTransition.captureEnd(transitionValues.view, transitionValues.values)
        }

        override fun onAppear(sceneRoot: ViewGroup, view: View, startValues: TransitionValuesFramework?, endValues: TransitionValuesFramework?): Animator? {
            generalizedTransition.onBeforeCreateAnimator(sceneRoot, view, view, startValues?.values, endValues?.values)
            return generalizedTransition.createAnimator(sceneRoot, null, view, startValues?.values, endValues?.values)
        }

        override fun onDisappear(sceneRoot: ViewGroup, view: View, startValues: TransitionValuesFramework?, endValues: TransitionValuesFramework?): Animator? {
            generalizedTransition.onBeforeCreateAnimator(sceneRoot, view, view, startValues?.values, endValues?.values)
            return generalizedTransition.createAnimator(sceneRoot, view, null, startValues?.values, endValues?.values)
        }
    }

    private class VisibilitySupport(
            private val generalizedTransition: GeneralizedTransition
    ) : android.support.transition.Visibility() {

        override fun captureStartValues(transitionValues: TransitionValuesSupport) {
            super.captureStartValues(transitionValues)
            generalizedTransition.captureStart(transitionValues.view, transitionValues.values)
        }

        override fun captureEndValues(transitionValues: TransitionValuesSupport) {
            super.captureEndValues(transitionValues)
            generalizedTransition.captureEnd(transitionValues.view, transitionValues.values)
        }

        override fun onAppear(sceneRoot: ViewGroup, view: View, startValues: TransitionValuesSupport?, endValues: TransitionValuesSupport?): Animator? {
            if (endValues == null) {
                return null
            }
            generalizedTransition.onBeforeCreateAnimator(sceneRoot, view, view, startValues?.values, endValues.values)
            return generalizedTransition.createAnimator(sceneRoot, null, view, startValues?.values, endValues.values)
        }

        override fun onDisappear(sceneRoot: ViewGroup, view: View, startValues: TransitionValuesSupport?, endValues: TransitionValuesSupport?): Animator? {
            if (startValues == null) {
                return null
            }
            generalizedTransition.onBeforeCreateAnimator(sceneRoot, view, view, startValues.values, endValues?.values)
            return generalizedTransition.createAnimator(sceneRoot, view, null, startValues.values, endValues?.values)
        }
    }

    /**
     * Copy of internal system GhostUtils.
     */
    private class GhostViewImpl private constructor(
            private val mGhostView: View
    ) {

        fun setVisibility(visibility: Int) {
            mGhostView.visibility = visibility
        }

        companion object {

            private val TAG = "GhostViewApi21"

            private var sGhostViewClass: Class<*>? = null
            private var sGhostViewClassFetched: Boolean = false
            private var sAddGhostMethod: Method? = null
            private var sAddGhostMethodFetched: Boolean = false
            private var sRemoveGhostMethod: Method? = null
            private var sRemoveGhostMethodFetched: Boolean = false

            @SuppressLint("PrivateApi")
            private fun fetchGhostViewClass() {
                if (!sGhostViewClassFetched) {
                    try {
                        sGhostViewClass = Class.forName("android.view.GhostView")
                    } catch (e: ClassNotFoundException) {
                        Log.i(TAG, "Failed to retrieve GhostView class", e)
                    }

                    sGhostViewClassFetched = true
                }
            }

            private fun fetchAddGhostMethod() {
                if (!sAddGhostMethodFetched) {
                    try {
                        fetchGhostViewClass()
                        sAddGhostMethod = sGhostViewClass!!.getDeclaredMethod(
                                "addGhost", View::class.java,
                                ViewGroup::class.java, Matrix::class.java
                        )
                        sAddGhostMethod!!.isAccessible = true
                    } catch (e: NoSuchMethodException) {
                        Log.i(TAG, "Failed to retrieve addGhost method", e)
                    }

                    sAddGhostMethodFetched = true
                }
            }

            private fun fetchRemoveGhostMethod() {
                if (!sRemoveGhostMethodFetched) {
                    try {
                        fetchGhostViewClass()
                        sRemoveGhostMethod = sGhostViewClass!!.getDeclaredMethod("removeGhost", View::class.java)
                        sRemoveGhostMethod!!.isAccessible = true
                    } catch (e: NoSuchMethodException) {
                        Log.i(TAG, "Failed to retrieve removeGhost method", e)
                    }

                    sRemoveGhostMethodFetched = true
                }
            }

            fun addGhost(view: View, viewGroup: ViewGroup): GhostViewImpl? {
                fetchAddGhostMethod()
                if (sAddGhostMethod != null) {
                    try {
                        return GhostViewImpl(
                                sAddGhostMethod!!.invoke(null, view, viewGroup, null) as View
                        )
                    } catch (e: IllegalAccessException) {
                        // Do nothing
                    } catch (e: InvocationTargetException) {
                        throw RuntimeException(e.cause)
                    }
                }
                return null
            }

            fun removeGhost(view: View) {
                fetchRemoveGhostMethod()
                if (sRemoveGhostMethod != null) {
                    try {
                        sRemoveGhostMethod!!.invoke(null, view)
                    } catch (e: IllegalAccessException) {
                        // Do nothing
                    } catch (e: InvocationTargetException) {
                        throw RuntimeException(e.cause)
                    }
                }
            }
        }
    }
}
