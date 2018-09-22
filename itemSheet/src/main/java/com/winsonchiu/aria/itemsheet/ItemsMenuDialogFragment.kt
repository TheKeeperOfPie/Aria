package com.winsonchiu.aria.itemsheet

import android.animation.Animator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.core.animation.doOnStart
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.winsonchiu.aria.framework.fragment.FragmentArgument
import kotlinx.android.synthetic.main.items_menu_fragment.*

class ItemsMenuDialogFragment<DataType : ItemsMenuItem> : Fragment() {

    var items by FragmentArgument<ArrayList<DataType>>("items")

    companion object {
        val BACK_STACK_TAG = "${ItemsMenuDialogFragment::class.java.canonicalName}.TAG"

        private const val MILLISECONDS_PER_INCH = 25f

        fun <DataType : ItemsMenuItem> newInstance(items: List<DataType>) = ItemsMenuDialogFragment<DataType>().apply {
            this.items = (items as? ArrayList<DataType> ?: ArrayList(items))
        }
    }

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private lateinit var gestureDetector: GestureDetectorCompat

    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
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
                (parentFragment as Listener<DataType>).onClick(item)
                dismiss()
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

    fun show(fragmentManager: FragmentManager?, @IdRes containerId: Int) {
        fragmentManager
                ?.beginTransaction()
                ?.add(containerId, this, null)
                ?.addToBackStack(BACK_STACK_TAG)
                ?.commit()
    }

    private fun dismiss() {
        fragmentManager?.popBackStack(BACK_STACK_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
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

                        val speed = MILLISECONDS_PER_INCH / layoutBottomSheet.context.resources.displayMetrics.densityDpi
                        val distance = endTop - startTop
//                        duration = (distance * speed).toLong()
                    }
        }
    }

    interface Listener<DataType : ItemsMenuItem> {
        fun onClick(item: DataType)
    }
}