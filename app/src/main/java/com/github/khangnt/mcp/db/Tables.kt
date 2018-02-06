package com.github.khangnt.mcp.db

/**
 * Created by Khang NT on 1/2/18.
 * Email: khang.neon.1997@gmail.com
 */

interface JobTable {
    companion object {
        const val NAME = "jobs"
        const val C_ID = "_id"
        const val C_TITLE = "title"
        const val C_STATUS = "status"
        const val C_STATUS_DETAIL = "status_detail"
        const val C_COMMAND = "command"

        fun getCreateTableScript(): String =
                """
                CREATE TABLE IF NOT EXISTS $NAME (
                    $C_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $C_TITLE TEXT NOT NULL,
                    $C_STATUS INTEGER NOT NULL,
                    $C_STATUS_DETAIL TEXT,
                    $C_COMMAND TEXT NOT NULL
                )
                """
    }
}