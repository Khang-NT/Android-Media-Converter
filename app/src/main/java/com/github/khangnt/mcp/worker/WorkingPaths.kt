package com.github.khangnt.mcp.worker

import android.content.Context
import com.github.khangnt.mcp.*
import com.github.khangnt.mcp.util.ensureDirExists
import java.io.File

/**
 * Created by Khang NT on 1/22/18.
 * Email: khang.neon.1997@gmail.com
 */

data class WorkingPaths(
        val tempDir: File,
        val fileDir: File,
        val jobTempRootDir: File = File(tempDir, JOB_TEMP_FOLDER).ensureDirExists(),
        val jobLogRootDir: File = File(fileDir, JOB_LOG_FOLDER).ensureDirExists(),
        val ffmpegPath: File = File(fileDir, FFMPEG_FILE)
) {
    fun getTempDirForJob(jobId: Long): File = File(jobTempRootDir, jobId.toString()).ensureDirExists()

    fun getLogFileOfJob(jobId: Long): File = File(jobLogRootDir, "$jobId.log")
}

/**
 * Working paths can change any times (app move to other storage, sdcard mount/unmounted,...)
 * Jobs which affected when working paths changed should be fail with message:
 * "Some app data deleted or moved."
 */
fun makeWorkingPaths(context: Context): WorkingPaths {
    val fileDir = context.getDir(APP_FILE_FOLDER, Context.MODE_PRIVATE)
    val tempDir: File = try {
        val dir = context.getExternalFilesDir(APP_TEMP_FOLDER).ensureDirExists()
        if (dir.canWrite()) dir else context.getDir(APP_TEMP_FOLDER, Context.MODE_PRIVATE)
    } catch (ignore: Throwable) {
        // fallback to fileDir anyway
        context.getDir(APP_TEMP_FOLDER, Context.MODE_PRIVATE)
    }

    return WorkingPaths(tempDir, fileDir)
}

fun makeInputTempFile(jobTempDir: File, inputIndex: Int): File = File(jobTempDir, "input$inputIndex")