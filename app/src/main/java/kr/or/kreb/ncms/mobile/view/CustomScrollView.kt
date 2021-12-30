/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView
import android.view.MotionEvent

class CustomScrollView : ScrollView {

    private var enableScrolling = true

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    private fun isEnableScrolling(): Boolean {
        return enableScrolling
    }

    fun setEnableScrolling(enableScrolling: Boolean) {
        this.enableScrolling = enableScrolling
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return if (isEnableScrolling()) {
            super.onInterceptTouchEvent(ev)
        } else {
            false
        }
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return if (isEnableScrolling()) {
            super.onTouchEvent(ev)
        } else {
            false
        }
    }
}