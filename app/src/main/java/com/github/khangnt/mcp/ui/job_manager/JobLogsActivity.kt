package com.github.khangnt.mcp.ui.job_manager

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.worker.WorkingPaths
import com.github.khangnt.mcp.worker.makeWorkingPaths
import kotlinx.android.synthetic.main.activity_job_logs.*
import java.io.File

class JobLogsActivity : AppCompatActivity() {

    private var logFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_logs)

        // just for testing
        val workingPaths: WorkingPaths = makeWorkingPaths(applicationContext)
        workingPaths.getLogFileOfJob(1)

        val logs: String = logFile?.readText() ?: ""
        tvLogs.text = logs
    }
}
