package com.github.khangnt.mcp

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import com.github.khangnt.mcp.ui.filepicker.FileBrowserViewModel
import com.github.khangnt.mcp.ui.jobmaker.JobMakerViewModel
import com.github.khangnt.mcp.ui.jobmanager.JobManagerViewModel
import java.lang.IllegalArgumentException

/**
 * Created by Khang NT on 4/3/18.
 * Email: khang.neon.1997@gmail.com
 */

class ViewModelFactory(private val appContext: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = with(modelClass) {
        when {
            isAssignableFrom(JobManagerViewModel::class.java) -> JobManagerViewModel(appContext)
            isAssignableFrom(FileBrowserViewModel::class.java) -> FileBrowserViewModel()
            isAssignableFrom(JobMakerViewModel::class.java) -> JobMakerViewModel()
            else -> throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
        } as T
    }

}