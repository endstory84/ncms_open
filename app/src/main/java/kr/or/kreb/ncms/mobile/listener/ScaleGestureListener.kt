/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.listener

import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View

class ScaleGestureListener : View.OnTouchListener, ScaleGestureDetector.OnScaleGestureListener {

    private var view: View? = null
    private val gestureScale: ScaleGestureDetector? = null
    private var scaleFactor = 1f
    private var inScale = false


    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        this.view = v
        gestureScale?.onTouchEvent(event)
        return true
    }

    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        scaleFactor *= detector!!.scaleFactor
        scaleFactor = if (scaleFactor < 1) 1f else scaleFactor // prevent our view from becoming too small //

        scaleFactor = (scaleFactor * 100).toInt().toFloat() / 100 // Change precision to help with jitter when user just rests their fingers //

        view!!.scaleX = scaleFactor
        view!!.scaleY = scaleFactor
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        inScale = true
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
        inScale = false
    }
}