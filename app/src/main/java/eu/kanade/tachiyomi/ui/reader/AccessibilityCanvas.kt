package eu.kanade.tachiyomi.ui.reader

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.forEach
import androidx.customview.widget.ExploreByTouchHelper
import eu.kanade.tachiyomi.ui.reader.model.AccessibilityItem
import java.util.*

@SuppressLint("ClickableViewAccessibility")
class AccessibilityCanvas(context: Context, attr: AttributeSet?) :
    View(context, attr) {

    var firstPoint: Point? = null
    var paint = Paint().also {
        it.color = Color.BLUE
        it.strokeWidth = 10f
        it.style = Paint.Style.STROKE
    }
    var rect: Rect? = null
    private val accessHelper = AccessHelper()
    var list = mutableListOf<AccessibilityItem>()
    var gambeta = LinkedList(listOf("World Trigger", "Doctor Stone"))

    init {
        ViewCompat.setAccessibilityDelegate(this, accessHelper)
        setOnTouchListener { _, event -> draw(event) }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        list.forEach {
            canvas?.run {
                drawRect(it.rect, paint)
            }
        }

        rect?.let {
            canvas?.run {
                drawRect(it, paint)
            }
        }
    }

    private fun draw(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                firstPoint = Point(event.x.toInt(), event.y.toInt())
            }
            MotionEvent.ACTION_MOVE -> {
                rect = Rect(firstPoint!!.x, firstPoint!!.y, event.x.toInt(), event.y.toInt())
            }
            MotionEvent.ACTION_UP -> {
                if (gambeta.size > 0) {
                    rect?.let { list.add(AccessibilityItem(it, gambeta.pop())) }
                    rect = null
                }
            }
        }
        invalidate()
        return true
    }

    inner class AccessHelper : ExploreByTouchHelper(this) {
        override fun getVirtualViewAt(x: Float, y: Float): Int {
            list.forEach {
                if (it.rect.contains(x.toInt(), y.toInt())) {
                    return list.indexOf(it)
                }
            }
            return -1
        }

        override fun getVisibleVirtualViews(virtualViewIds: MutableList<Int>?) {
            virtualViewIds?.addAll(0 until list.count())
        }

        @Suppress("DEPRECATION")
        override fun onPopulateNodeForVirtualView(virtualViewId: Int, node: AccessibilityNodeInfoCompat) {
            val item = list[virtualViewId]
            node.className = AccessibilityCanvas::class.simpleName
            node.contentDescription = item.text
            node.setBoundsInParent(item.rect)
        }

        override fun onPerformActionForVirtualView(virtualViewId: Int, action: Int, arguments: Bundle?): Boolean = false
    }
}
