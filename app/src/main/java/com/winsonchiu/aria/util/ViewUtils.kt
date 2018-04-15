package com.winsonchiu.aria.util

import android.content.Context
import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.ButterKnife
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airbnb.epoxy.TypedEpoxyController

fun <T : ViewGroup> T.initialize(@LayoutRes layoutRes: Int) {
    View.inflate(context, layoutRes, this)
    ButterKnife.bind(this)
}

fun <T> TypedEpoxyController<T>.setDataForView(recyclerView: EpoxyRecyclerView, data: T) {
    setData(data)
    if (recyclerView.adapter != adapter) {
        recyclerView.setController(this)
    }
}

fun <T> TypedEpoxyController<T>.setDataForView(recyclerView: RecyclerView, data: T) {
    setData(data)
    if (recyclerView.adapter != adapter) {
        recyclerView.adapter = adapter
    }
}

fun TextView.textOrGone(text: CharSequence?) {
    this.text = text
    visibility = if (text.isNullOrBlank()) {
        View.GONE
    } else {
        View.VISIBLE
    }
}

fun Float.dpToPx(view: View) = dpToPx(view.context)
fun Float.dpToPx(context: Context) = dpToPx(context.resources.displayMetrics)
fun Float.dpToPx(displayMetrics: DisplayMetrics) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, displayMetrics)