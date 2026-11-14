package com.solvay.Utils

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.animation.OvershootInterpolator
import com.solvay.R

@SuppressLint("ClickableViewAccessibility")
class SlidingPanelHelper(
    private val view: View,
    private val initialMinHeight: Int = 35,
    private val initialMaxHeight: Int = 400
) {
    var minHeight: Int = initialMinHeight
        set(value) {
            field = value.coerceAtLeast(0)
            if (view.height > field) {
                updatePanelHeight(field)
            }
        }

    var maxHeight: Int = initialMaxHeight
        set(value) {
            field = value.coerceAtLeast(minHeight)
            if (view.height < field && isExpanded) {
                updatePanelHeight(field)
            }
        }

    private var isExpanded = false
    private val animDuration = 300L
    private var initialY = 0f
    private var initialHeight = 0

    init {
        view.findViewById<View>(R.id.straightLine).setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialY = event.rawY
                    initialHeight = view.height
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaY = initialY - event.rawY
                    val newHeight = (initialHeight + deltaY).coerceIn(minHeight.toFloat(), maxHeight.toFloat())
                    updatePanelHeight(newHeight.toInt())
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val velocityTracker = VelocityTracker.obtain()
                    velocityTracker.addMovement(event)
                    velocityTracker.computeCurrentVelocity(1000)

                    val velocityY = velocityTracker.yVelocity
                    animateToNearestState(velocityY)
                    velocityTracker.recycle()
                    true
                }
                else -> false
            }
        }
    }

    private fun updatePanelHeight(newHeight: Int) {
        view.layoutParams.height = newHeight.coerceIn(minHeight, maxHeight)
        view.requestLayout()
    }

    fun toggle() {
        animatePanel(!isExpanded)
    }

    private fun animateToNearestState(velocityY: Float) {
        val currentHeight = view.height
        val shouldExpand = when {
            velocityY < -1000 -> true  // Fast swipe up
            velocityY > 1000 -> false  // Fast swipe down
            else -> currentHeight > (maxHeight + minHeight) / 2
        }
        animatePanel(shouldExpand)
    }

    private fun animatePanel(expand: Boolean) {
        val targetHeight = if (expand) {
            measureContentHeight().coerceAtMost(maxHeight)
        } else {
            minHeight
        }

        isExpanded = expand

        ValueAnimator.ofInt(view.height, targetHeight).apply {
            duration = animDuration
            interpolator = OvershootInterpolator(0.8f)
            addUpdateListener {
                updatePanelHeight(it.animatedValue as Int)
            }
            start()
        }
    }


    private fun measureContentHeight(): Int {
        view.measure(
            View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.UNSPECIFIED
        )
        return view.measuredHeight
    }
}