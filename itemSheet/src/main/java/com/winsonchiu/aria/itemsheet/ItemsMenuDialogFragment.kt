package com.winsonchiu.aria.itemsheet

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import androidx.core.animation.doOnStart
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.postDelayed
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.winsonchiu.aria.dialog.DialogActivityFragment
import com.winsonchiu.aria.framework.fragment.FragmentArgument
import kotlinx.android.synthetic.main.items_menu_fragment.*

class ItemsMenuDialogFragment<DataType : ItemsMenuItem> : DialogActivityFragment() {

    companion object {

        val KEY_RESULT_ITEM = "${ItemsMenuDialogFragment::class.java.canonicalName}.resultItem"

        fun <DataType : ItemsMenuItem> newInstance(items: List<DataType>) = ItemsMenuDialogFragment<DataType>().apply {
            this.items = (items as? ArrayList<DataType> ?: ArrayList(items))
        }
    }

    private var items by FragmentArgument<ArrayList<DataType>>("items")

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private lateinit var gestureDetector: GestureDetectorCompat

    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            dismiss()
            return true
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.items_menu_fragment, container, false)

    override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        gestureDetector = GestureDetectorCompat(context, gestureListener)

        recyclerItems.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        recyclerItems.adapter = ItemsMenuAdapter(items, object : ItemsMenuAdapter.Listener<DataType> {

            @Suppress("UNCHECKED_CAST")
            override fun onClick(item: DataType) {
                val data = Intent().apply {
                    putExtra(KEY_RESULT_ITEM, item)
                }
                deliverResultAndDismiss(data)
            }
        })

        viewBackground.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }

        bottomSheetBehavior = BottomSheetBehavior.from(layoutBottomSheet)
        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.skipCollapsed = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(
                    bottomSheet: View,
                    slideOffset: Float
            ) {
            }

            override fun onStateChanged(
                    bottomSheet: View,
                    newState: Int
            ) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    dismiss()
                }
            }
        })
    }

    override fun onCreateAnimator(
            transit: Int,
            enter: Boolean,
            nextAnim: Int
    ): Animator {
        val layoutBottomSheet = layoutBottomSheet
        val viewBackground = viewBackground

        if (enter) {
            return ValueAnimator.ofFloat(0f, 1f)
                    .apply {
                        doOnStart {
                            bottomSheetBehavior.peekHeight = 0

                            layoutBottomSheet.postDelayed(50) {
                                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                            }
                        }

                        addUpdateListener {
                            viewBackground.alpha = it.animatedFraction
                        }

                        interpolator = FastOutSlowInInterpolator()
                    }
        } else {
            val startTop = layoutBottomSheet.top
            val endTop = layoutCoordinator.height

            return ValueAnimator.ofInt(startTop, endTop)
                    .apply {
                        addUpdateListener {
                            layoutBottomSheet.top = it.animatedValue as Int
                            viewBackground.alpha = 1f - it.animatedFraction
                        }

                        interpolator = AccelerateInterpolator()
                    }
        }
    }
}