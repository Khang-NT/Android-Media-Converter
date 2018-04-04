package com.github.khangnt.mcp.db.job

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.github.khangnt.mcp.annotation.JobStatus

@Entity(tableName = "jobs_v2", indices = [Index("status")])
data class Job(
        @ColumnInfo(name = "_id") @PrimaryKey(autoGenerate = true) val id: Long = 0,
        @ColumnInfo(name = "title") val title: String,
        @ColumnInfo(name = "status") @JobStatus val status: Int = JobStatus.PENDING,
        @ColumnInfo(name = "status_detail") val statusDetail: String? = null,
        @ColumnInfo(name = "command") val command: Command
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Job

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    fun isDone(): Boolean = status == JobStatus.FAILED || status == JobStatus.COMPLETED
}
