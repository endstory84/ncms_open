/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.include_wtncc_info_view.view.*
import kotlinx.android.synthetic.main.inlcude_cadastral_info_view.view.*

class InfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    layout: Int
) : LinearLayout(context, attrs, layout) {

    init { inflate(context,layout, this) }

    fun setText(data: String, type:String){
            when(type){
                "cadastral" -> {
                    if (data.isEmpty()) tvCadastralLayerInfo.text = "" else tvCadastralLayerInfo.text = data
                }
                else -> {
                    if (data.isEmpty()) tvWtnccLayerInfo.text = "" else tvWtnccLayerInfo.text = data
                }
            }

    }

}