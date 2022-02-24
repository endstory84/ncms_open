/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.enums

/**
 * 스케치
 */

enum class SketchEnum(val value:Int) {
    UNDO(0),    // 이전
    REDO(1),    // 다음
    POINT(2),   // 스케치 유형 (점)
    LINE(3),    // 스케치 유형 (선)
    MODIFY(4),  // 편집(수정)
    REMOVE(5),  // 삭제
    CANCEL(6),  // 취소
    POLYGON(7)  // 완료(저장)
}