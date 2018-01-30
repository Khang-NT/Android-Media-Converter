package com.github.khangnt.mcp.ui.filepicker

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.BaseFragment
import com.github.khangnt.mcp.ui.common.IdGenerator
import com.github.khangnt.mcp.ui.common.MixAdapter
import com.github.khangnt.mcp.util.onTextSizeChanged
import com.github.khangnt.mcp.util.toast
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_file_list.*
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.Comparator

/**
 * Created by Khang NT on 1/28/18.
 * Email: khang.neon.1997@gmail.com
 */

class FileListFragment : BaseFragment() {
    companion object {
        fun newInstance(path: File, fileSelectable: Boolean, onSelectListener: (File) -> Unit): FileListFragment {
            return FileListFragment().apply {
                this.path = path
                this.fileSelectable = fileSelectable
                this.onSelectListener = onSelectListener
            }
        }
    }

    private lateinit var path: File
    private lateinit var onSelectListener: (File) -> Unit
    private var fileSelectable: Boolean = false

    private var selected: FileListModel? = null

    private val idGenerator = IdGenerator.scope("FileListFragment")
    private val createFolderAdapterModel = FileListModel(File("Dummy"), TYPE_CREATE_FOLDER,
            idGenerator.idFor("Dummy").toLong())

    private var adapter: MixAdapter? = null

    private val onClickListener = { model: FileListModel, pos: Int ->
        when (model.type) {
            TYPE_FOLDER -> onSelectListener(model.path)
            TYPE_FILE -> {
                if (fileSelectable) {
                    val oldPos = selected?.let { adapter?.indexOf(it) } ?: -1
                    selected = model

                    if (oldPos >= 0) adapter?.notifyItemChanged(oldPos)
                    adapter?.notifyItemChanged(pos)
                }
                onSelectListener(model.path)
            }
            TYPE_CREATE_FOLDER -> showCreateFolderDialog()
        }
    }

    private val selectedIdRetriever: () -> Long? = { selected?.modelId }

    init {
        retainInstance = true
    }

    fun getFileSelected(): File? = selected?.path

    fun clearSelected() {
        selected = null
        adapter?.notifyDataSetChanged()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_file_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewGroup.onRetry = this::reload
        recyclerViewGroup.recyclerView?.layoutManager = LinearLayoutManager(context!!)

        if (adapter == null) {
            adapter = MixAdapter.Builder(context!!)
                    .register(FileListModel::class.java, { inflater, parent ->
                        FileListViewHolder(
                                inflater.inflate(R.layout.item_file_list, parent, false),
                                onClickListener, selectedIdRetriever
                        )
                    })
                    .build()
            adapter!!.setHasStableIds(true)
        }
        recyclerViewGroup.recyclerView?.adapter = adapter

        if (adapter?.itemCount == 0) {
            reload()
        } else {
            recyclerViewGroup.successWithData()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerViewGroup.recyclerView?.adapter = null
    }

    private fun reload() {
        adapter?.let { adapter ->
            recyclerViewGroup.loading()
            Observable
                    .defer {
                        val listFile: Array<File> = path.listFiles() ?: emptyArray()
                        return@defer Observable.fromArray(*listFile)
                    }
                    .map { file ->
                        FileListModel(file, if (file.isDirectory) TYPE_FOLDER else TYPE_FILE,
                                idGenerator.idFor(file.absolutePath).toLong())
                    }
                    .toList()
                    .map { list ->
                        Collections.sort(list, fileListComparator)
                        return@map list
                    }
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ listModel ->
                        if (path.canWrite()) {
                            listModel.add(0, createFolderAdapterModel)
                        }
                        adapter.setData(listModel)
                        if (listModel.isEmpty()) {
                            recyclerViewGroup.empty()
                        } else {
                            recyclerViewGroup.successWithData()
                        }
                    }, { error ->
                        Timber.d(error)
                        recyclerViewGroup.error(error.message)
                    })
                    .disposeOnViewDestroyed()
        }
    }

    private fun showCreateFolderDialog() {
        val editText = EditText(context!!)
        val paddingVertical = resources.getDimensionPixelSize(R.dimen.margin_normal)
        val paddingHorizontal = resources.getDimensionPixelSize(R.dimen.margin_huge)
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
                            ?.setMargins(paddingVertical, paddingVertical, paddingVertical, paddingVertical)

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
                                reload()
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