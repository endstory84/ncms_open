/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import kr.or.kreb.ncms.mobile.data.SearchKeyword

@Dao
interface NcmsDAO {
    @Query("SELECT seq, keyword FROM keyword ORDER BY seq DESC")
    fun getAll(): LiveData<List<SearchKeyword>>

    @Insert(onConflict = REPLACE)
    fun insert(vararg data: SearchKeyword)

    @Delete
    fun delete(vararg data: SearchKeyword)

    @Query("DELETE FROM keyword")
    fun deleteAll()

    @Query("DELETE FROM keyword WHERE seq = :seq")
    suspend fun deleteSeq(seq:Long)

}