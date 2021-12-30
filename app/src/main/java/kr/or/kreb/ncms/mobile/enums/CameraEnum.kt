/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.enums

/**
 * 카메라
 */

enum class CameraEnum(val value:Int) {
    DEFAULT(0), // 일반(현장조사)
    DOCUMENT(1), // 문서스캔(신분증,등본,사업자등록증 등등)
}