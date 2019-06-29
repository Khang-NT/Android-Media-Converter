package com.github.khangnt.mcp.ui.filepicker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.BaseFragment
import com.github.khangnt.mcp.ui.common.MixAdapter
import com.github.khangnt.mcp.util.getViewModel
import com.github.khangnt.mcp.util.onTextSizeChanged
import com.github.khangnt.mcp.util.toast
import kotlinx.android.synthetic.main.fragment_file_browser.*
import java.io.File


private const val KEY_LIMIT_SELECT_COUNT = "FileBrowserFragment:limit_select_count"
private const val KET_START_UP_DIR = "FileBrowserFragment:start_up_directory"


class FileBrowserFragment : BaseFragment() {

    interface Callbacks {
        fun onSelectFilesChanged(files: List<File>)
        fun onCurrentDirectoryChanged(directory: File)
        fun allowChangeSelectedFile(): Boolean
    }

    companion object {
        fun newInstance(
                startUpDirectory: File,
                limitSelectCount: Int = 1
        ) = FileBrowserFragment().apply {
            arguments = Bundle().apply {
                putString(KET_START_UP_DIR, startUpDirectory.absolutePath)
                putInt(KEY_LIMIT_SELECT_COUNT, limitSelectCount)
            }
        }
    }

    private val limitSelectCount: Int by lazy { arguments!!.getInt(KEY_LIMIT_SELECT_COUNT) }
    private val startUpDirectory by lazy { File(arguments!!.getString(KET_START_UP_DIR)) }

    private val viewModel by lazy { getViewModel<FileBrowserViewModel>() }
    private val adapter: MixAdapter by lazy {
        MixAdapter.Builder {
            withModel<FileListModel> {
                FileListViewHolder.Factory {
                    onClickListener = { model, _ -> onFileClick(model) }
                }
            }
        }.build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (viewModel.getCurrentDirectory() == null) {
            viewModel.setCurrentDirectory(startUpDirectory)
            getCallbacks()?.onCurrentDirectoryChanged(startUpDirectory)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_file_browser, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(recyclerViewContainer) {
            onRefreshListener = { viewModel.reload(); true }
            getRecyclerView().adapter = adapter
            getRecyclerView().layoutManager = LinearLayoutManager(requireContext())
        }
        viewModel.getFileModels().observe {
            adapter.setData(it)
            recyclerViewContainer.setShowEmptyState(it.isEmpty())
        }
        viewModel.getStatus().observe {
            recyclerViewContainer.setStatus(it)
        }
    }

    private fun onFileClick(model: FileListModel) {
        if (model.type == TYPE_FOLDER) {
            viewModel.setCurrentDirectory(model.path)
            getCallbacks()?.onCurrentDirectoryChanged(model.path)
        } else if (model.type == TYPE_CREATE_FOLDER) {
            showCreateFolderDialog()
        } else if (getCallbacks()?.allowChangeSelectedFile() != false) {
            if (model.selected) {
                // unselected it
                viewModel.unselectedFile(model.path)
                refreshOnSelectedFilesChanged()
            } else {
                val isFull = viewModel.getSelectedFiles().size == limitSelectCount
                if (isFull && limitSelectCount != 1) {
                    toast(getString(R.string.hint_limit_file_select_count, limitSelectCount))
                } else {
                    if (isFull && limitSelectCount == 1) {
                        viewModel.unselectedFile(viewModel.getSelectedFiles()[0])
                    }
                    viewModel.selectFile(model.path)
                    refreshOnSelectedFilesChanged()
                }
            }
        } else {
            showFileSelectionLockedMessage()
        }
    }

    private fun refreshOnSelectedFilesChanged() {
        getCallbacks()?.onSelectFilesChanged(viewModel.getSelectedFiles())
    }

    private fun showFileSelectionLockedMessage() {
        toast(R.string.message_disallow_change_selected_files)
    }

    fun goto(dir: File) {
        if (dir.isFile) {
            goto(dir.parentFile)
        } else {
            viewModel.setCurrentDirectory(dir)
            getCallbacks()?.onCurrentDirectoryChanged(dir)
        }
    }

    fun reset() {
        viewModel.discardSelectedFiles()
        getCallbacks()?.onSelectFilesChanged(emptyList())
    }

    fun getCurrentDirectory(): File = viewModel.getCurrentDirectory() ?: startUpDirectory

    fun getSelectedFiles() = viewModel.getSelectedFiles()

    fun setSelectedFiles(files: List<File>) = viewModel.setSelectedFiles(files)

    fun selectAllFilesInCurrentFolder() {
        if (getCallbacks()?.allowChangeSelectedFile() != false) {
            viewModel.getFileModels().value.orEmpty().forEach { model ->
                val isFull = viewModel.getSelectedFiles().size == limitSelectCount
                if (model.type == TYPE_FILE
                        && !isFull
                        && !model.selected) {
                    viewModel.selectFile(model.path)
                }
            }
            refreshOnSelectedFilesChanged()
        } else {
            showFileSelectionLockedMessage()
        }
    }

    fun deselectAllFilesInCurrentFolder() {
        if (getCallbacks()?.allowChangeSelectedFile() != false) {
            viewModel.getFileModels().value.orEmpty().forEach { model ->
                if (model.type == TYPE_FILE && model.selected) {
                    viewModel.unselectedFile(model.path)
                }
            }
            refreshOnSelectedFilesChanged()
        } else {
            showFileSelectionLockedMessage()
        }
    }

    private fun getCallbacks(): Callbacks? {
        return (activity as? Callbacks) ?: (parentFragment as Callbacks)
    }

    override fun onBackPressed(): Boolean {
        viewModel.goBack()?.let {
            getCallbacks()?.onCurrentDirectoryChanged(it)
            return true
        }
        return super.onBackPressed()
    }

    private fun showCreateFolderDialog() {
        val editText = EditText(context!!)
        val padding = resources.getDimensionPixelSize(R.dimen.margin_normal)
        editText.hint = getString(R.string.hint_type_folder_name)

        AlertDialog.Builder(context!!)
                .setView(editText)
                .setTitle(R.string.create_new_folder)
                .setPositiveButton(R.string.action_ok, null)
                .setNegativeButton(R.string.action_cancel, null)
                .show()
                .apply {
                    val okButton = getButton(AlertDialog.BUTTON_POSITIVE)
                    okButton.isEnabled = false
                    editText.onTextSizeChanged { length -> okButton.isEnabled = length in 1..49 }
                    (editText.layoutParams as? ViewGroup.MarginLayoutParams)
                            ?.setMargins(padding, padding, padding, padding)

                    okButton.setOnClickListener {
                        val folderName = editText.text.toString()
                        val newFolderPath = File(viewModel.getCurrentDirectory(), folderName)
                        try {
                            if (newFolderPath.exists()) {
                                toast(R.string.folder_exists)
                            } else if (!newFolderPath.mkdir()) {
                                toast(R.string.create_folder_failed)
                            } else {
                                toast(getString(R.string.create_folder_success, folderName))
                                viewModel.reload()
                                dismiss()
                            }
                        } catch (error: Throwable) {
                            toast(error.message)
                        }
                    }
                }
    }

}