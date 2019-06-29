package com.github.khangnt.mcp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.khangnt.mcp.ui.filepicker.FileBrowserViewModel
import com.github.khangnt.mcp.ui.jobmaker.JobMakerViewModel
import com.github.khangnt.mcp.ui.jobmaker.selectoutput.ChooseOutputViewModel
import com.github.khangnt.mcp.ui.jobmanager.JobManagerViewModel

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
            isAssignableFrom(ChooseOutputViewModel::class.java) -> ChooseOutputViewModel()
            else -> throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
        } as T
    }

}