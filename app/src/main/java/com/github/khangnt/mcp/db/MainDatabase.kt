package com.github.khangnt.mcp.db

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.arch.persistence.room.migration.Migration
import com.github.khangnt.mcp.db.job.Job
import com.github.khangnt.mcp.db.job.JobDao


//const val DB_VERSION = 1
const val DB_VERSION = 2
const val DB_NAME = "main_db"

@Database(entities = [Job::class], version = DB_VERSION, exportSchema = true)
@TypeConverters(com.github.khangnt.mcp.db.TypeConverters::class)
abstract class MainDatabase : RoomDatabase() {
    abstract fun getJobDao(): JobDao
}

class Migration1To2 : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `jobs_v2` (
                `_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `title` TEXT NOT NULL,
                `status` INTEGER NOT NULL,
                `status_detail` TEXT,
                `command` TEXT NOT NULL
            )
        """)

        database.execSQL("""
            CREATE  INDEX `index_jobs_v2_status` ON `jobs_v2` (`status`)
        """)

        database.execSQL("""
            INSERT INTO jobs_v2 (_id, title, status_detail, command, status)
            SELECT _id, title, status_detail, command, CASE status
                    WHEN 0 THEN 5
                    WHEN 1 THEN 2
                    WHEN 2 THEN 1
                    WHEN 3 THEN 0
                    WHEN 5 THEN 3
                    ELSE status
                END status
            FROM jobs
            """)

        database.execSQL("DROP TABLE jobs") // drop `jobs` version 1
    }
}