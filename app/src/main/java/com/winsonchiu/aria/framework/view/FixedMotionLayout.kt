package com.winsonchiu.aria.framework.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.winsonchiu.aria.R
import com.winsonchiu.aria.framework.util.horizontalDimensionFixed

class FixedMotionLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : MotionLayout(context, attrs, defStyleAttr) {


    override fun addView(
            child: View?,
            params: ViewGroup.LayoutParams?
    ) {
        super.addView(child, params)
    }

    override fun addView(
            child: View?,
            index: Int,
            params: ViewGroup.LayoutParams?
    ) {
        if (child?.id == R.id.textSongDescription) {
            params as ConstraintLayout.LayoutParams
            params.constrainedWidth = true
            params.matchConstraintDefaultWidth = 1
            params.horizontalDimensionFixed(false)
        }
        super.addView(child, index, params)
    }

    override fun setConstraintSet(set: ConstraintSet?) {
        super.setConstraintSet(set)

        set ?: return
        val method = ConstraintSet::class.java.getDeclaredMethod("get", Int::class.javaPrimitiveType).apply {
            isAccessible = true
        }

        val constraint = method.invoke(set, R.id.textSongDescription) as ConstraintSet.Constraint
        constraint.constrainedWidth = true
        constraint.widthDefault = 1
    }
}