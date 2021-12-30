/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.data

import org.json.JSONObject

data class Biz(
    //@PrimaryKey(autoGenerate = true) val seq: Int,
    val saupCode:String,
    val contCode:String,
    val bsnsCl:String,
    val bsnsClNm:String,
    val bsnsNm:String,
    val bsnsLocplc:String,
    val bsnsPsCl:String,
    val bsnsPsClNm:String,
    val oclhgBnoraCl:String,
    val oclhgBnoraClNm:String,
    val bsnsLclasCl:String,
    val oclhgBnora:String,
    val excAceptncAt:String,
    val excUseAt:String,
    val excDtls:String,
    val ncm:String,
    val cntrctDe:String,
    val item: JSONObject
)