package com.winsonchiu.aria.framework.util

import android.content.Context
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import butterknife.ButterKnife
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airbnb.epoxy.SimpleEpoxyController
import com.airbnb.epoxy.TypedEpoxyController
import com.winsonchiu.aria.framework.BuildConfig
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.math.roundToInt

fun <T : ViewGroup> T.initialize(@LayoutRes layoutRes: Int) {
    View.inflate(context, layoutRes, this)
    ButterKnife.bind(this)
}

fun <T> TypedEpoxyController<T>.setDataForView(
        recyclerView: EpoxyRecyclerView,
        data: T
) {
    setData(data)
    if (recyclerView.adapter != adapter) {
        recyclerView.setController(this)
    }
}

fun SimpleEpoxyController.setDataForView(
        recyclerView: EpoxyRecyclerView,
        data: List<EpoxyModel<*>>
) {
    setModels(data)
    if (recyclerView.adapter != adapter) {
        recyclerView.setController(this)
    }
}

fun <T> TypedEpoxyController<T>.setDataForView(
        recyclerView: RecyclerView,
        data: T
) {
    setData(data)
    if (recyclerView.adapter != adapter) {
        recyclerView.adapter = adapter
    }
}

var TextView.textOrGone: CharSequence?
    get() = text
    set(value) {
        this.text = value
        visibility = if (text.isNullOrBlank()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

fun Int.dpToPx(view: View) = dpToPx(view.context)
fun Int.dpToPx(context: Context) = dpToPx(context.resources.displayMetrics)
fun Int.dpToPx(displayMetrics: DisplayMetrics) = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        displayMetrics
).roundToInt()

fun Float.dpToPx(view: View) = dpToPx(view.context)
fun Float.dpToPx(context: Context) = dpToPx(context.resources.displayMetrics)
fun Float.dpToPx(displayMetrics: DisplayMetrics) = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        displayMetrics
)


fun Int.spToPx(view: View) = spToPx(view.context)
fun Int.spToPx(context: Context) = spToPx(context.resources.displayMetrics)
fun Int.spToPx(displayMetrics: DisplayMetrics) = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this.toFloat(),
        displayMetrics
).roundToInt()

fun Float.spToPx(view: View) = spToPx(view.context)
fun Float.spToPx(context: Context) = spToPx(context.resources.displayMetrics)
fun Float.spToPx(displayMetrics: DisplayMetrics) = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this,
        displayMetrics
)


fun ViewGroup.findChild(block: (child: View) -> Boolean): View? {
    children.forEach {
        if (it is ViewGroup) {
            it.findChild(block)?.let { return it }
        }

        if (block(it)) {
            return it
        }
    }

    return null
}

private var constraintLayoutUpdateHierarchyMethod: Method? = null

fun ConstraintLayout.forceUpdateHierarchy() {
    try {
        if (constraintLayoutUpdateHierarchyMethod == null) {
            constraintLayoutUpdateHierarchyMethod = ConstraintLayout::class.java.getDeclaredMethod("setChildrenConstraints")
            constraintLayoutUpdateHierarchyMethod?.isAccessible = true
        }

        constraintLayoutUpdateHierarchyMethod?.invoke(this)
    } catch (ignored: Exception) {
        if (BuildConfig.DEBUG) {
            throw ignored
        }
    }
}

private var constraintLayoutHorizontalDimensionFixedField: Field? = null

fun ConstraintLayout.LayoutParams.horizontalDimensionFixed(fixed: Boolean) {
    try {
        if (constraintLayoutHorizontalDimensionFixedField == null) {
            constraintLayoutHorizontalDimensionFixedField = ConstraintLayout.LayoutParams::class.java.getDeclaredField("horizontalDimensionFixed")
            constraintLayoutHorizontalDimensionFixedField?.isAccessible = true
        }

        constraintLayoutHorizontalDimensionFixedField?.set(this, fixed)
    } catch (ignored: Exception) {
        if (BuildConfig.DEBUG) {
            throw ignored
        }
    }
}

fun ConstraintLayout.LayoutParams.getHorizontalDimensionFixed(): Boolean {
    try {
        if (constraintLayoutHorizontalDimensionFixedField == null) {
            constraintLayoutHorizontalDimensionFixedField = ConstraintLayout.LayoutParams::class.java.getDeclaredField("horizontalDimensionFixed")
            constraintLayoutHorizontalDimensionFixedField?.isAccessible = true
        }

        return constraintLayoutHorizontalDimensionFixedField?.get(this) as Boolean
    } catch (ignored: Exception) {
        if (BuildConfig.DEBUG) {
            throw ignored
        }
    }

    return true
}