package com.winsonchiu.aria.framework.util.animation.transition

import android.graphics.Rect
import android.transition.Transition
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.winsonchiu.aria.BuildConfig
import java.lang.reflect.Method

private var SET_TRANSITION_ALPHA_METHOD: Method? = null
private var SUPPRESS_LAYOUT_METHOD: Method? = null

/**
 * Internal utils for custom transition work. These should generally not be used anywhere else,
 * but are left public so a refactor to a separate module wasn't necessary.
 */

fun View.setLeftTopRightBottom(left: Int, top: Int, right: Int, bottom: Int) {
    setLeft(left)
    setTop(top)
    setRight(right)
    setBottom(bottom)
}

fun View.setLeftTopRightBottom(rect: Rect) = setLeftTopRightBottom(rect.left, rect.top, rect.right, rect.bottom)

fun ViewGroup.suppressLayout(suppress: Boolean) {
    try {
        if (SUPPRESS_LAYOUT_METHOD == null) {
            SUPPRESS_LAYOUT_METHOD = ViewGroup::class.java.getDeclaredMethod("suppressLayout", java.lang.Boolean.TYPE)
        }

        SUPPRESS_LAYOUT_METHOD?.invoke(this, suppress)
    } catch (ignored: Exception) {
        if (BuildConfig.DEBUG) {
            throw ignored
        }
    }
}

fun View.setTransitionAlpha(alpha: Float) {
    try {
        if (SET_TRANSITION_ALPHA_METHOD == null) {
            SET_TRANSITION_ALPHA_METHOD = View::class.java.getDeclaredMethod("setTransitionAlpha", java.lang.Float.TYPE)
        }

        SET_TRANSITION_ALPHA_METHOD?.invoke(this, alpha)
    } catch (ignored: Exception) {
        if (BuildConfig.DEBUG) {
            throw ignored
        }
    }
}

class WindowTransitionHolder(
        private val sharedElementEnterTransition: Transition? = null,
        private val sharedElementReturnTransition: Transition? = null,
        private val enterTransition: Transition? = null,
        private val returnTransition: Transition? = null,
        private val transitionBackgroundFadeDuration: Long = 0,
        private val allowEnterTransitionOverlap: Boolean = true,
        private val allowReturnTransitionOverlap: Boolean = true
) {

    fun applyTo(window: Window) {
        window.sharedElementEnterTransition = sharedElementEnterTransition
        window.sharedElementReturnTransition = sharedElementReturnTransition
        window.enterTransition = enterTransition
        window.returnTransition = returnTransition
        window.transitionBackgroundFadeDuration = transitionBackgroundFadeDuration
        window.allowEnterTransitionOverlap = allowEnterTransitionOverlap
        window.allowReturnTransitionOverlap = allowReturnTransitionOverlap
    }
}

class FragmentTransitionHolder<in PreviousFragment : Fragment, in NewFragment : Fragment>(
        private val previousExitTransition: androidx.transition.Transition? = null,
        private val previousReenterTransition: androidx.transition.Transition? = null,
        private val newSharedElementEnterTransition: androidx.transition.Transition? = null,
        private val newSharedElementReturnTransition: androidx.transition.Transition? = null,
        private val newEnterTransition: androidx.transition.Transition? = null,
        private val newReturnTransition: androidx.transition.Transition? = null
) {

    fun applyTo(previousFragment: PreviousFragment, newFragment: NewFragment) {
        previousFragment.exitTransition = previousExitTransition
        previousFragment.reenterTransition = previousReenterTransition
        previousFragment.allowEnterTransitionOverlap = true
        previousFragment.allowReturnTransitionOverlap = true

        applyToNew(newFragment)
    }

    fun applyToNew(newFragment: NewFragment) {
        newFragment.sharedElementEnterTransition = newSharedElementEnterTransition
        newFragment.sharedElementReturnTransition = newSharedElementReturnTransition
        newFragment.enterTransition = newEnterTransition
        newFragment.returnTransition = newReturnTransition
        newFragment.allowEnterTransitionOverlap = true
        newFragment.allowReturnTransitionOverlap = true
    }
}

class TransitionUtils {
    companion object {
        fun makeParentLayoutSupressionTransitionListener(
                view: View?,
                innerListener: GeneralizedTransitionListener? = null,
                removeFunction: () -> Unit
        ): GeneralizedTransitionListener? {
            return (view?.parent as? ViewGroup)?.let {
                it.suppressLayout(true)
                return object : GeneralizedTransitionListener(innerListener) {
                    private var canceled = false

                    override fun onTransitionEnd() {
                        if (!canceled) {
                            it.suppressLayout(false)
                        }
                        removeFunction()
                    }

                    override fun onTransitionCancel() {
                        it.suppressLayout(false)
                        canceled = true
                    }

                    override fun onTransitionPause() {
                        it.suppressLayout(false)
                    }

                    override fun onTransitionResume() {
                        it.suppressLayout(true)
                    }
                }
            }
        }

        fun performFragmentTransitionWithViewRemoval(previousFragment: Fragment, vararg excludedViews: View?, block: (FragmentTransaction) -> Unit) {
            val viewGroup = previousFragment.view as? ViewGroup ?: return
            val fragmentManager = previousFragment.fragmentManager ?: return

            val transaction = fragmentManager.beginTransaction()

            block(transaction)

//            fragmentManager.executePendingTransactions()

            /*
                To prevent the system from using low res snapshots, we can remove them
                from the hierarchy.
            */
//            viewGroup.postDelayed(1) {
//                (viewGroup.childCount - 1 downTo 0)
//                        .map(viewGroup::getChildAt)
//                        .filterNot(excludedViews::contains)
//                        .forEach(viewGroup::removeView)
//            }
        }
    }
}
