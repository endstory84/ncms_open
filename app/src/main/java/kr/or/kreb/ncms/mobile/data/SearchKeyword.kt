/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "keyword")
data class SearchKeyword (
    @PrimaryKey(autoGenerate = true) val seq: Int,
    val keyword: String? = ""
)