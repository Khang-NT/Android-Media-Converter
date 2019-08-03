package com.github.khangnt.mcp.worker

import android.content.Context
import com.github.khangnt.mcp.*
import com.github.khangnt.mcp.util.ensureDirExists
import com.github.khangnt.mcp.util.isLocked
import com.github.khangnt.mcp.util.listFilesNotNull
import java.io.File

/**
 * Created by Khang NT on 1/22/18.
 * Email: khang.neon.1997@gmail.com
 */

data class WorkingPaths(
        val tempDir: File,
        val fileDir: File,
        val ffmpegPath: File = File(fileDir, FFMPEG_FILE)
) {
    private val jobTempRootDir: File by lazy { File(tempDir, JOB_TEMP_FOLDER).ensureDirExists() }
    private val jobLogRootDir: File by lazy { File(fileDir, JOB_LOG_FOLDER).ensureDirExists() }

    fun getTempDirForJob(jobId: Long): File = File(jobTempRootDir, jobId.toString()).ensureDirExists()

    fun getListJobTempDir(): Array<File> = jobTempRootDir.listFilesNotNull()

    fun getLogFileOfJob(jobId: Long): File = File(jobLogRootDir, "$jobId.log")

    fun getAllLogFiles(): List<File> {
        return jobLogRootDir.listFilesNotNull()
                .filter { file -> file.isFile && file.extension == "log" && !file.isLocked() }
    }
}

/**
 * Working paths can change any times (app move to other storage, sdcard mount/unmounted,...)
 * Jobs which affected when working paths changed should be fail with message:
 * "Some app data deleted or moved."
 */
fun makeWorkingPaths(context: Context): WorkingPaths {
    val fileDir = context.getDir(APP_FILE_FOLDER, Context.MODE_PRIVATE)
    val tempDir: File = try {
        context.getExternalFilesDir(APP_TEMP_FOLDER)!!.ensureDirExists()
    } catch (ignore: Throwable) {
        // fallback to fileDir anyway
        context.getDir(APP_TEMP_FOLDER, Context.MODE_PRIVATE)
    }

    return WorkingPaths(tempDir, fileDir)
}

fun makeInputTempFile(jobTempDir: File, inputIndex: Int): File = File(jobTempDir, "input$inputIndex")