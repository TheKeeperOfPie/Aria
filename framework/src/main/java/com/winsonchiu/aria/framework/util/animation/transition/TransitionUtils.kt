package com.winsonchiu.aria.framework.util.animation.transition

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.transition.TransitionValues
import com.winsonchiu.aria.framework.BuildConfig
import java.lang.reflect.Method

private var SET_TRANSITION_ALPHA_METHOD: Method? = null
private var SUPPRESS_LAYOUT_METHOD: Method? = null

/**
 * Internal utils for custom transition work. These should generally not be used anywhere else,
 * but are left public so a refactor to a separate module wasn't necessary.
 */

fun View.setLeftTopRightBottom(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
) {
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

operator fun TransitionValues.component1() = view
operator fun TransitionValues.component2() = values

object TransitionUtils {
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

    fun performFragmentTransitionWithViewRemoval(
            previousFragment: Fragment,
            vararg excludedViews: View?,
            block: (FragmentTransaction) -> Unit
    ) {
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
