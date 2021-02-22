package com.udacity.project4

import android.view.View
import com.udacity.project4.TextViewDrawableMatcher.DrawableMatcher
import org.hamcrest.Matcher


class EspressoTestMatchers {
    fun withDrawable(resourceId: Int): Matcher<View?>? {
        return DrawableMatcher(resourceId)
    }
}