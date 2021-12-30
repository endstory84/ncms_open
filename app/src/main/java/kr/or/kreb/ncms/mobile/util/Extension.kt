/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.util

import android.view.View
import android.view.ViewGroup

//fun String?.checkStringNull():String { this.apply { if (this == "null") "" else { this } }

fun View.goneView() {
    this.run { post { visibility = View.GONE } }
}

fun View.invisibleView() {
    this.run { post { visibility = View.GONE } }
}

fun View.visibleView() {
    this.run { post { visibility = View.VISIBLE } }
}

fun View.forEachChildView(closure: (View) -> Unit) {
    closure(this)
    val groupView = this as? ViewGroup ?: return
    val size = groupView.childCount - 1
    for (i in 0..size) {
        groupView.getChildAt(i).forEachChildView(closure)
    }
}

