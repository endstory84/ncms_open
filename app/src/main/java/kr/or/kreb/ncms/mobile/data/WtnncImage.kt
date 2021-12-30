/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.data

import android.graphics.Bitmap

data class WtnncImage(
        var seq:Int, // 순번
        var image: Bitmap?,
        var saupCode:String,
        var bizCode:String,
        var rmTxt:String,
        var fileNameString:String,
        var fileCode:String,
        var fileCodeNm:String,
        var lon:String,
        var lat:String,
        var azimuth:String
    )