/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.adapter

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.naver.maps.map.overlay.InfoWindow
import kr.or.kreb.ncms.mobile.R

class NaverMapContextAdapter(val context: Context) : InfoWindow.ViewAdapter() {

    private var rootView : View? = null

    private var icon1 : ImageView? = null
    private var text1 : TextView? = null

    private var icon2 : ImageView? = null
    private var text2 : TextView? = null

    private var icon3 : ImageView? = null
    private var text3 : TextView? = null

    private var layout1 : LinearLayout? = null
    private var layout2 : LinearLayout? = null
    private var layout3 : LinearLayout? = null

    override fun getView(infoWindow: InfoWindow): View {
        val view = rootView ?: View.inflate(context, R.layout.view_map_context_menu, null).also {
            rootView = it
        }
        layout1 = view.findViewById(R.id.contextmenu1)
        layout2 = view.findViewById(R.id.contextmenu2)
        layout3 = view.findViewById(R.id.contextmenu3)

        text1 = view.findViewById(R.id.text) as TextView
        var button1 = view.findViewById(R.id.button1) as Button


        text1!!.text ="등록`11111111111111111111"

        text1!!.setOnClickListener {
            Log.e("chkim","1111111111111111111111111111111등록 클릭");
        }
//        text1!!.setOnClickListener {
//            Log.e("chkim","1111111111111111111111111111111등록 클릭");
//        }


        return view;
    }


}