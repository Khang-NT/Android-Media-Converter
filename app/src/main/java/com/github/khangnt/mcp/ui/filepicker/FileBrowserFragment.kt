package com.github.khangnt.mcp.ui.filepicker

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.SingletonInstances
import com.github.khangnt.mcp.ui.BaseFragment
import com.github.khangnt.mcp.ui.common.MixAdapter
import com.github.khangnt.mcp.util.onTextSizeChanged
import com.github.khangnt.mcp.util.toast
import kotlinx.android.synthetic.main.fragment_file_browser.*
import java.io.File


private const val KEY_LIMIT_SELECT_COUNT = "FileBrowserFragment:limit_select_count"
private const val KET_START_UP_DIR = "FileBrowserFragment:start_up_directory"


class FileBrowserFragment : BaseFragment(){

    interface Callbacks {
        fun onSelectFilesChanged(files: List<File>)
        fun onCurrentDirectoryChanged(directory: File)
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

    private lateinit var viewModel: FileBrowserViewModel
    private lateinit var adapter: MixAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, SingletonInstances.getViewModelFactory())
                .get(FileBrowserViewModel::class.java)
        if (viewModel.getCurrentDirectory() == null) {
            viewModel.setCurrentDirectory(startUpDirectory)
            getCallbacks()?.onCurrentDirectoryChanged(startUpDirectory)
        }

        adapter = MixAdapter.Builder {
            withModel<FileListModel> {
                FileListViewHolder.Factory {
                    onClickListener = { model, _ ->
                        if (model.type == TYPE_FOLDER) {
                            viewModel.setCurrentDirectory(model.path)
                            getCallbacks()?.onCurrentDirectoryChanged(model.path)
                        } else if (model.type == TYPE_CREATE_FOLDER) {
                            showCreateFolderDialog()
                        } else if (model.selected) {
                            // unselected it
                            viewModel.unselectedFile(model.path)
                            getCallbacks()?.onSelectFilesChanged(viewModel.getSelectedFiles())
                        } else {
                            val isFull = viewModel.getSelectedFiles().size == limitSelectCount
                            if (isFull && limitSelectCount != 1) {
                                toast(getString(R.string.hint_limit_file_select_count, limitSelectCount))
                            } else {
                                if (isFull && limitSelectCount == 1) {
                                    viewModel.unselectedFile(viewModel.getSelectedFiles()[0])
                                }
                                viewModel.selectFile(model.path)
                                getCallbacks()?.onSelectFilesChanged(viewModel.getSelectedFiles())
                            }
                        }
                    }
                }
            }
        }.build()
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

    fun goto(dir: File) {
        if (dir.isFile) {
            goto(dir.parentFile)
        } else {
            viewModel.setCurrentDirectory(dir)
            getCallbacks()?.onCurrentDirectoryChanged(dir)
        }
    }


    fun getCurrentDirectory(): File = viewModel.getCurrentDirectory() ?: startUpDirectory

    fun getSelectedFiles() = viewModel.getSelectedFiles()

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