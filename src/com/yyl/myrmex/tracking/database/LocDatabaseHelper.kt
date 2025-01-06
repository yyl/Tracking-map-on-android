package com.yyl.myrmex.tracking.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class LocDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        LocTable.onCreate(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        LocTable.onUpgrade(db, oldVersion, newVersion)
    }

    companion object {
        private const val DATABASE_NAME = "loctable.db"
        private const val DATABASE_VERSION = 1
        const val DEBUG_TAG = "location table"
    }
}
