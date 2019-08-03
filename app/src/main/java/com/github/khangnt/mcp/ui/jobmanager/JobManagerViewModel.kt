package com.github.khangnt.mcp.ui.jobmanager

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.SingletonInstances
import com.github.khangnt.mcp.annotation.JobStatus
import com.github.khangnt.mcp.db.job.Job
import com.github.khangnt.mcp.misc.RunningJobStatus
import com.github.khangnt.mcp.ui.common.AdapterModel
import com.github.khangnt.mcp.ui.common.HeaderModel
import com.github.khangnt.mcp.ui.common.Status
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit

/**
 * Created by Khang NT on 4/3/18.
 * Email: khang.neon.1997@gmail.com
 */

class JobManagerViewModel(appContext: Context) : ViewModel() {
    private val runningHeaderModel = LiveHeaderModel(appContext.getString(R.string.header_running_job),
            RunningJobStatus.observeRunningJobStatus())
    private val preparingHeaderModel = HeaderModel(appContext.getString(R.string.header_preparing_job))
    private val readyHeaderModel = HeaderModel(appContext.getString(R.string.header_ready_job))
    private val pendingHeaderModel = HeaderModel(appContext.getString(R.string.header_pending_job))
    private val finishedHeaderModel = HeaderModel(appContext.getString(R.string.header_finished_job))

    private val adapterModelLiveData = MutableLiveData<List<AdapterModel>>()
    private val jobLoadStatus = MutableLiveData<Status>()
    private var disposable: Disposable? = null


    fun reload() {
        adapterModelLiveData.value = emptyList()
        jobLoadStatus.value = Status.Loading
        disposable?.dispose() // dispose previous load if it still running
        with(SingletonInstances.getJobRepository()) {
            disposable = Flowable.combineLatest(getIncompleteJobs(), getCompletedJobs(), combineFunction())
                    .throttleLast(400, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ newJobModels ->
                        adapterModelLiveData.value = newJobModels
                        // after first update, return to Idle status to hide "loading"
                        jobLoadStatus.value = Status.Idle
                    }, { error ->
                        adapterModelLiveData.value = emptyList()
                        jobLoadStatus.value = Status.Error(error, handled = false)
                    })
        }
    }

    private fun combineFunction() = BiFunction<List<Job>, List<Job>, List<AdapterModel>> { list1, list2 ->
        val listModels = mutableListOf<AdapterModel>()
        var previousStatus: Int? = null
        list1.forEach { item ->
            if (item.status != previousStatus) {
                previousStatus = item.status
                when {
                    item.status == JobStatus.RUNNING -> listModels.add(runningHeaderModel)
                    item.status == JobStatus.PREPARING -> listModels.add(preparingHeaderModel)
                    item.status == JobStatus.READY -> listModels.add(readyHeaderModel)
                    item.status == JobStatus.PENDING -> listModels.add(pendingHeaderModel)
                }
            }
            listModels.add(JobModel(item))
        }
        if (list2.isNotEmpty()) {
            listModels.add(finishedHeaderModel)
            listModels.addAll(list2.map { JobModel(it) })
        }
        return@BiFunction listModels
    }

    fun getAdapterModel(): LiveData<List<AdapterModel>> = adapterModelLiveData

    fun getStatus(): LiveData<Status> = jobLoadStatus

    override fun onCleared() {
        super.onCleared()
        disposable?.dispose()
    }
}