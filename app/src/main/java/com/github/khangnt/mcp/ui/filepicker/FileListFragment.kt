package com.github.khangnt.mcp.ui.filepicker

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.BaseFragment
import com.github.khangnt.mcp.ui.common.MixAdapter
import com.github.khangnt.mcp.util.onTextSizeChanged
import com.github.khangnt.mcp.util.toast
import com.github.khangnt.mcp.view.RecyclerViewGroupState
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_file_list.*
import timber.log.Timber
import java.io.File

typealias OnItemClickListener = (
        fragment: FileListFragment,
        item: File
) -> Boolean

typealias CheckedFilesRetriever = () -> List<File>

private const val KEY_PATH = "FileListFragment:path"

class FileListFragment : BaseFragment() {
    companion object {
        fun newInstance(path: File): FileListFragment {
            return FileListFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_PATH, path.absolutePath)
                }
            }
        }
    }

    private val path: File by lazy { File(arguments!!.getString(KEY_PATH)) }
    private val createFolderItem = FileListModel(File("+folder"), TYPE_CREATE_FOLDER)

    private lateinit var adapter: MixAdapter
    private lateinit var recyclerViewGroupState: RecyclerViewGroupState

    lateinit var onItemClickListener: OnItemClickListener
    lateinit var checkedFilesRetriever: CheckedFilesRetriever

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        if (!(::onItemClickListener.isInitialized && ::checkedFilesRetriever.isInitialized)) {
            throw IllegalStateException("Must initialize onItemClickListener & checkedFilesRetriever")
        }

        adapter = MixAdapter.Builder(context!!, "FileListFragment")
                .register(FileListModel::class.java, { inflater, parent ->
                    FileListViewHolder(
                            inflater.inflate(R.layout.item_file_list, parent, false),
                            onClickListener, checkStateFunc
                    )
                })
                .build()
        recyclerViewGroupState = RecyclerViewGroupState().setRetryFunc(this::reloadData)
        reloadData()
    }

    private val onClickListener = { model: FileListModel, _: Int ->
        when (model.type) {
            TYPE_FOLDER, TYPE_FILE -> {
                if (onItemClickListener(this, model.path)) {
                    adapter.notifyDataSetChanged()
                }
            }
            TYPE_CREATE_FOLDER -> showCreateFolderDialog()
        }
    }

    private val checkStateFunc: (FileListModel) -> Boolean = { checkedFilesRetriever().contains(it.path) }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_file_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewGroupState.bind(recyclerViewGroup, adapter)
    }

    private fun reloadData() {
        recyclerViewGroupState.loading()
        Observable
                .defer {
                    val listFile: Array<File> = path.listFiles() ?: emptyArray()
                    return@defer Observable.fromArray(*listFile)
                }
                .map { FileListModel(it, if (it.isDirectory) TYPE_FOLDER else TYPE_FILE) }
                .toSortedList(fileListComparator)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ listModel ->
                    if (path.canWrite()) {
                        listModel.add(0, createFolderItem)
                    }
                    adapter.setData(listModel)
                    recyclerViewGroupState.checkData(listModel)
                }, { error ->
                    Timber.d(error)
                    recyclerViewGroupState.error(error.message)
                })
                .disposeOnDestroyed(tag = "reloadData")
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
                        val newFolderPath = File(path, folderName)
                        try {
                            if (newFolderPath.exists()) {
                                toast(R.string.folder_exists)
                            } else if (!newFolderPath.mkdir()) {
                                toast(R.string.create_folder_failed)
                            } else {
                                toast(getString(R.string.create_folder_success, folderName))
                                reloadData()
                                dismiss()
                            }
                        } catch (error: Throwable) {
                            toast(error.message)
                        }
                    }
                }
    }

    private val fileListComparator: Comparator<FileListModel> = Comparator { f1, f2 ->
        when {
            f1.type == f2.type -> f1.path.name.compareTo(f2.path.name, ignoreCase = true)
            f1.type == TYPE_FOLDER -> -1
            else -> 1
        }
    }
}