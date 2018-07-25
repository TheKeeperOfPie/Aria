package com.winsonchiu.aria.main

import android.content.Context
import android.support.constraint.motion.MotionLayout
import android.support.v4.view.MotionEventCompat
import android.support.v4.view.ViewCompat
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.doOnNextLayout
import com.winsonchiu.aria.R
import com.winsonchiu.aria.framework.util.dpToPx

class MainLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var viewDragHelper: ViewDragHelper
    private lateinit var viewNowPlaying: MotionLayout

    private var downX = 0f
    private var downY = 0f

    private var dragOffset = 0f

    override fun onFinishInflate() {
        super.onFinishInflate()

        viewNowPlaying = findViewById(R.id.viewNowPlaying)

        viewDragHelper = ViewDragHelper.create(this, 1f, object : ViewDragHelper.Callback() {
            override fun tryCaptureView(
                    child: View,
                    pointerId: Int
            ): Boolean {
                return child == viewNowPlaying
            }

            override fun getViewVerticalDragRange(child: View) = getDragRange()

            override fun clampViewPositionVertical(
                    child: View,
                    top: Int,
                    dy: Int
            ) = (top + dy).coerceIn(0, getDragRange())

            override fun onViewPositionChanged(
                    changedView: View,
                    left: Int,
                    top: Int,
                    dx: Int,
                    dy: Int
            ) {
                super.onViewPositionChanged(changedView, left, top, dx, dy)
                dragOffset = top / getDragRange().toFloat()
                (changedView as MotionLayout).progress = top / (changedView.height - 56f.dpToPx(changedView))
            }

            override fun onViewReleased(
                    releasedChild: View,
                    xvel: Float,
                    yvel: Float
            ) {
                var top = paddingTop
                if (yvel > 0 || yvel == 0f && dragOffset > 0.5f) {
                    top += getDragRange()
                }
                viewDragHelper.settleCapturedViewAt(releasedChild.left, top)

                ViewCompat.postInvalidateOnAnimation(this@MainLayout)
            }
        })

        viewNowPlaying.doOnNextLayout {
            Log.d("MainLayout", "doOnNextLayout called with ${getDragRange()}")
            viewNowPlaying.offsetTopAndBottom(getDragRange())
            viewNowPlaying.progress = 1f
        }
    }

    private fun getDragRange(): Int {
        return viewNowPlaying.height - 56.dpToPx(context)
    }

    override fun computeScroll() {
        super.computeScroll()

        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    fun smoothSlideTo(slideOffset: Float): Boolean {
//        val topBound = paddingTop
//        val y = (topBound + slideOffset * getDragRange())
//
//        if (viewDragHelper.smoothSlideViewTo(viewNowPlaying, viewNowPlaying.left, y.toInt())) {
//            ViewCompat.postInvalidateOnAnimation(this)
//            return true
//        }
        return false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val action = MotionEventCompat.getActionMasked(ev)

        if (action != MotionEvent.ACTION_DOWN) {
            viewDragHelper.cancel()
            return super.onInterceptTouchEvent(ev)
        }

        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            viewDragHelper.cancel()
            return false
        }

        val x = ev.x
        val y = ev.y
        var interceptTap = false

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                downX = x
                downY = y
                interceptTap = viewDragHelper.isViewUnder(viewNowPlaying, x.toInt(), y.toInt())
            }

            MotionEvent.ACTION_MOVE -> {
                val adx = Math.abs(x - downX)
                val ady = Math.abs(y - downY)
                val slop = viewDragHelper.touchSlop
                if (ady > slop && adx > ady) {
                    viewDragHelper.cancel()
                    return false
                }
            }
        }

        return viewDragHelper.shouldInterceptTouchEvent(ev) || interceptTap
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        viewDragHelper.processTouchEvent(ev)

        val action = ev.action
        val x = ev.x
        val y = ev.y

        val isHeaderViewUnder = viewDragHelper.isViewUnder(viewNowPlaying, x.toInt(), y.toInt())
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                downX = x
                downY = y
            }

            MotionEvent.ACTION_UP -> {
                val dx = x - downX
                val dy = y - downY
                val slop = viewDragHelper.touchSlop
                if (dx * dx + dy * dy < slop * slop && isHeaderViewUnder) {
                    if (viewNowPlaying.progress == 1f) {
                        smoothSlideTo(0f)
                    }
                }
            }
        }


        return isHeaderViewUnder && isViewHit(viewNowPlaying, x.toInt(), y.toInt())
    }


    private fun isViewHit(
            view: View,
            x: Int,
            y: Int
    ): Boolean {
        val viewLocation = IntArray(2)
        view.getLocationOnScreen(viewLocation)
        val parentLocation = IntArray(2)
        this.getLocationOnScreen(parentLocation)
        val screenX = parentLocation[0] + x
        val screenY = parentLocation[1] + y
        return screenX >= viewLocation[0] && screenX < viewLocation[0] + view.width &&
                screenY >= viewLocation[1] && screenY < viewLocation[1] + view.height
    }
}