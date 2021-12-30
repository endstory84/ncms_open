/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.enums

/**
 * 스케치
 */

enum class SketchEnum(val value:Int) {
    UNDO(0), // 이전
    REDO(1), // 다음
    MODIFY(2), // 편집(수정)
    REMOVE(3), // 삭제
    ADD(4), // 추가
    CANCEL(4), // 추가
    POLYGON(5) // 완료(저장)
}