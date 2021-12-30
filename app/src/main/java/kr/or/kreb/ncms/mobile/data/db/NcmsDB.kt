/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kr.or.kreb.ncms.mobile.data.SearchKeyword
import kr.or.kreb.ncms.mobile.data.db.dao.NcmsDAO

@Database(entities = [SearchKeyword::class], version = 1)
abstract class NcmsDB : RoomDatabase() {
    abstract fun dao(): NcmsDAO

    companion object {
        private var INSTANCE: NcmsDB? = null

        fun getInstance(context: Context): NcmsDB? {
            if (INSTANCE == null) {
                synchronized(NcmsDB::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        NcmsDB::class.java, "ncms.db"
                    ).allowMainThreadQueries().build()
                }
            }
            return INSTANCE
        }
    }
}