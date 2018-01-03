package com.github.khangnt.mcp.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Created by Khang NT on 1/2/18.
 * Email: khang.neon.1997@gmail.com
 */

const val DB_VERSION = 1
const val DB_NAME = "main_db"

class MainSqliteOpenHelper(
        context: Context,
        name: String = DB_NAME,
        version: Int = DB_VERSION
) : SQLiteOpenHelper(context, name, null, version) {

    override fun onCreate(db: SQLiteDatabase) {
        // CREATE Job Table
        db.execSQL(JobTable.getCreateTableScript())
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // empty
    }

}