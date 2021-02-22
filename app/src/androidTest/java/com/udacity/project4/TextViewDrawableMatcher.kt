package com.udacity.project4

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import timber.log.Timber

class TextViewDrawableMatcher {
    companion object {
        fun withDrawable(resourceId: Int): Matcher<View> = DrawableMatcher(resourceId)

        fun noDrawable(): Matcher<View> = DrawableMatcher(-1)
    }

    open class DrawableMatcher(private val resourceId: Int) : TypeSafeMatcher<View>() {
        private var expectedId = 0

        init {
            expectedId = resourceId
        }

        override fun describeTo(description: Description?) {
            description?.appendText("with drawable from resource id: ")
            description?.appendValue(expectedId)
        }

        override fun matchesSafely(item: View?): Boolean {
            if (!(item != null && item is TextView))
                return false

            val textView = item as TextView
            if (expectedId < 0)
                return textView.compoundDrawables == null

            val resources = item.getContext().resources
            val expectedDrawable = resources.getDrawable(expectedId) ?: return false
            Timber.i("CompoundDrawables: %s",textView.compoundDrawables.size)
            val bitmap = getBitmap(textView.compoundDrawables[1])
            val otherBitmap = getBitmap(expectedDrawable)
            return bitmap.sameAs(otherBitmap)
        }

        private fun getBitmap(drawable: Drawable): Bitmap {
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }
    }
}