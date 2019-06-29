package com.github.khangnt.mcp.ui.filepicker

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.khangnt.mcp.ui.common.Status
import com.github.khangnt.mcp.util.listFilesNotNull
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.io.File
import java.util.*

/**
 * Created by Khang NT on 4/4/18.
 * Email: khang.neon.1997@gmail.com
 */

class FileBrowserViewModel : ViewModel() {

    private val createFolderItem = FileListModel(File("+folder"), TYPE_CREATE_FOLDER, false)

    private val backStack = Stack<File>()
    private var currentDirectory: File? = null

    private val selectedFiles = mutableListOf<File>()
    private val selectedFilesReadOnly = Collections.unmodifiableList(selectedFiles)

    private val directorySubject = BehaviorSubject.create<File>()
    private val selectedFileSubject = BehaviorSubject.createDefault(Unit)

    private val fileModelsLiveData = MutableLiveData<List<FileListModel>>()
    private val statusLiveData = MutableLiveData<Status>()

    private val disposable: Disposable

    init {
        val onDirectoryChanged = directorySubject.toFlowable(BackpressureStrategy.LATEST)
                .observeOn(Schedulers.computation())
                .map<List<FileListModel>> { directory ->
                    statusLiveData.postValue(Status.Loading)
                    val files = directory.listFilesNotNull().map {
                        val type = if (it.isDirectory) TYPE_FOLDER else TYPE_FILE
                        FileListModel(it, type)
                    }.toMutableList()
                    files.sortWith(fileListComparator)
                    if (directory.canWrite()) {
                        files.add(0, createFolderItem)
                    }
                    statusLiveData.postValue(Status.Idle)
                    return@map files
                }

        val onSelectedFileChanged = selectedFileSubject.toFlowable(BackpressureStrategy.LATEST)
                .observeOn(Schedulers.computation())

        disposable = Flowable
                .combineLatest(onDirectoryChanged, onSelectedFileChanged, updateSelectedStateFunction())
                .onErrorReturn {
                    Timber.d(it)
                    statusLiveData.postValue(Status.Error(it))
                    mutableListOf()
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { computedFileModels ->
                    fileModelsLiveData.value = computedFileModels
                }
    }

    private fun updateSelectedStateFunction() =
            BiFunction<List<FileListModel>, Unit, List<FileListModel>> { list, _ ->
                list.forEach { model ->
                    model.selected = selectedFiles.contains(model.path)
                }
                return@BiFunction list
            }


    fun setCurrentDirectory(path: File) {
        if (path != currentDirectory) {
            currentDirectory?.let { backStack.push(it) }
            currentDirectory = path
            directorySubject.onNext(path)
        }
    }

    fun getCurrentDirectory(): File? = currentDirectory

    fun goBack(): File? {
        if (!backStack.empty()) {
            return backStack.pop().also {
                currentDirectory = it
                directorySubject.onNext(it)
            }
        }
        return null
    }

    fun reload() {
        directorySubject.onNext(currentDirectory!!)
    }

    fun getStatus(): LiveData<Status> = statusLiveData

    fun getFileModels(): LiveData<List<FileListModel>> = fileModelsLiveData

    @MainThread
    fun selectFile(file: File) {
        selectedFiles.add(file)
        selectedFileSubject.onNext(Unit)
    }

    fun unselectedFile(file: File) {
        if (selectedFiles.remove(file)) {
            selectedFileSubject.onNext(Unit)
        }
    }

    fun discardSelectedFiles() {
        selectedFiles.clear()
        selectedFileSubject.onNext(Unit)
    }

    fun getSelectedFiles(): List<File> = selectedFilesReadOnly

    fun setSelectedFiles(files: List<File>) {
        selectedFiles.clear()
        selectedFiles.addAll(files)
        selectedFileSubject.onNext(Unit)
    }

    private val fileListComparator: Comparator<FileListModel> = Comparator { f1, f2 ->
        when {
            f1.type == f2.type -> f1.path.name.compareTo(f2.path.name, ignoreCase = true)
            f1.type == TYPE_FOLDER -> -1
            else -> 1
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }

}