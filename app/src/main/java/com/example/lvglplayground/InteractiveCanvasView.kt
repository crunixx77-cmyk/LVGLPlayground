package com.example.lvglplayground

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeProvider

class InteractiveCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val transformMatrix = Matrix()
    private var scaleFactor = 1.0f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var activePointerId = MotionEvent.INVALID_POINTER_ID

    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = scaleFactor.coerceIn(0.5f, 5.0f)
            transformMatrix.setScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
            invalidate()
            return true
        }
    })

    data class AccessibleElement(
        val id: Int,
        val textDescription: String,
        val boundsOnScreen: Rect,
        val isClickable: Boolean
    )

    private val elements = mutableListOf<AccessibleElement>()

    fun updateAccessibilityElements(newElements: List<AccessibleElement>) {
        elements.clear()
        elements.addAll(newElements)
        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                activePointerId = event.getPointerId(0)
            }
            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = event.findPointerIndex(activePointerId)
                if (pointerIndex != -1) {
                    val x = event.getX(pointerIndex)
                    val y = event.getY(pointerIndex)

                    if (!scaleDetector.isInProgress) {
                        val dx = x - lastTouchX
                        val dy = y - lastTouchY
                        transformMatrix.postTranslate(dx, dy)
                        invalidate()
                    }

                    lastTouchX = x
                    lastTouchY = y
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                activePointerId = MotionEvent.INVALID_POINTER_ID
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.concat(transformMatrix)
        canvas.restore()
    }

    override fun getAccessibilityNodeProvider(): AccessibilityNodeProvider {
        return object : AccessibilityNodeProvider() {
            override fun createAccessibilityNodeInfo(virtualViewId: Int): AccessibilityNodeInfo? {
                if (virtualViewId == HOST_VIEW_ID) {
                    val root = AccessibilityNodeInfo.obtain(this@InteractiveCanvasView)
                    for (element in elements) {
                        root.addChild(this@InteractiveCanvasView, element.id)
                    }
                    return root
                }

                val element = elements.find { it.id == virtualViewId } ?: return null
                return AccessibilityNodeInfo.obtain(this@InteractiveCanvasView, virtualViewId).apply {
                    packageName = context.packageName
                    className = "android.view.View"
                    contentDescription = element.textDescription
                    text = element.textDescription
                    setBoundsInScreen(element.boundsOnScreen)
                    isFocusable = true
                    isClickable = element.isClickable
                    isVisibleToUser = true
                }
            }

            override fun performAction(virtualViewId: Int, action: Int, arguments: Bundle?): Boolean {
                return false
            }
        }
    }
}
