package com.github.khangnt.mcp.ui.filepicker

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearLayoutManager.HORIZONTAL
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.BaseFragment
import com.github.khangnt.mcp.ui.common.MixAdapter
import kotlinx.android.synthetic.main.fragment_file_browser.*
import java.io.File
import java.util.*

/**
 * Created by Khang NT on 1/31/18.
 * Email: khang.neon.1997@gmail.com
 */

private const val KEY_MAX_FILE_CAN_SELECT = "FileBrowserFragment:max_file_can_select"
private const val KEY_REMOVE_OLDEST = "FileBrowserFragment:remove_oldest"
private const val KET_START_UP_DIR = "FileBrowserFragment:start_up_directory"

private const val KEY_SELECTED_FILE_LIST = "FileBrowserFragment:selected_file_list"
private const val KEY_CURRENT_DIR = "FileBrowserFragment:current_dir"

class FileBrowserFragment : BaseFragment() {

    companion object {
        fun newInstance(
                startUpDir: File,
                maxFileCanSelect: Int = 1,
                removeOldest: Boolean = true
        ): FileBrowserFragment {
            return FileBrowserFragment().apply {
                arguments = Bundle().apply {
                    putString(KET_START_UP_DIR, startUpDir.absolutePath)
                    putInt(KEY_MAX_FILE_CAN_SELECT, maxFileCanSelect)
                    putBoolean(KEY_REMOVE_OLDEST, removeOldest)
                }
            }
        }
    }

    val maxFileCanSelect by lazy { arguments!!.getInt(KEY_MAX_FILE_CAN_SELECT) }
    val removeOldest by lazy { arguments!!.getBoolean(KEY_REMOVE_OLDEST) }
    val startUpDir by lazy { File(arguments!!.getString(KET_START_UP_DIR)) }

    var onSelectedFilesChanged: ((fragment: FileBrowserFragment, file: List<File>) -> Unit)? = null
    var onDirChanged: ((fragment: FileBrowserFragment, file: File) -> Unit)? = null

    private val selectedFiles = mutableListOf<File>()
    private val selectedFilesReadOnly = Collections.unmodifiableList(selectedFiles)

    private var currentDir: File? = null
    private lateinit var pathIndicatorAdapter: MixAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (selectedFiles.isEmpty() && savedInstanceState !== null) {
            val list = savedInstanceState.getStringArrayList(KEY_SELECTED_FILE_LIST)
                    ?: emptyList<String>()
            selectedFiles.addAll(list.map { File(it) })
        }

        pathIndicatorAdapter = MixAdapter.Builder(context!!, "FileBrowserFragment")
                .register(PathIndicatorModel::class.java, { inflater, parent ->
                    val itemView = inflater.inflate(R.layout.item_path_indicator, parent, false)
                    return@register PathIndicatorViewHolder(itemView, onPathIndicatorClick)
                })
                .build()

        // re-attach callbacks
        childFragmentManager.fragments.forEach { fragment ->
            if (fragment is FileListFragment) {
                fragment.onItemClickListener = onItemClickListener
                fragment.checkedFilesRetriever = checkedFilesRetriever
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_file_browser, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pathIndicatorRecyclerView.layoutManager = LinearLayoutManager(context!!, HORIZONTAL, false)
        pathIndicatorRecyclerView.adapter = pathIndicatorAdapter

        if (currentDir === null) {
            val dir = savedInstanceState?.getString(KEY_CURRENT_DIR)?.let { File(it) } ?: startUpDir
            goto(dir)
        } else {
            updatePathIndicator()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pathIndicatorRecyclerView.adapter = null
    }

    fun goto(dir: File) {
        if (currentDir != dir) {
            val oldDir = currentDir
            currentDir = dir
            val existsFragment = childFragmentManager.findFragmentByTag(dir.absolutePath)
            if (existsFragment != null) {
                childFragmentManager.popBackStack(dir.absolutePath, 0)
            } else {
                val fragment = createFileListFragmentFor(dir)
                showFragment(oldDir, dir, fragment)
            }

            onDirChanged?.invoke(this, dir)
            updatePathIndicator()
        }
    }

    private fun createFileListFragmentFor(dir: File) = FileListFragment.newInstance(dir).also {
        // set callbacks
        it.onItemClickListener = onItemClickListener
        it.checkedFilesRetriever = checkedFilesRetriever
    }

    private val onItemClickListener: OnItemClickListener = { _, clickItem ->
        if (clickItem.isDirectory) {
            goto(clickItem)
            false
        } else if (selectedFiles.remove(clickItem)) {
            // discard select
            true
        } else if (selectedFiles.size < maxFileCanSelect) {
            // remember this file is selected
            selectedFiles.add(clickItem)
            true
        } else if (removeOldest && selectedFiles.isNotEmpty()) {
            // exceed max file can select -> remove oldest file
            selectedFiles.removeAt(0)
            selectedFiles.add(clickItem)
            true
        } else {
            // does nothing
            false
        }
    }

    private val checkedFilesRetriever: CheckedFilesRetriever = { selectedFilesReadOnly }

    private val onPathIndicatorClick = { model: PathIndicatorModel ->
        goto(model.path)
    }

    private fun updatePathIndicator() {
        val list = mutableListOf<PathIndicatorModel>()
        var path = currentDir
        while (path != null) {
            list.add(PathIndicatorModel(path))
            path = path.parentFile
        }
        pathIndicatorAdapter.setData(list.reversed())
    }

    private fun showFragment(oldDir: File?, dir: File, fileListFragment: FileListFragment) {
        childFragmentManager.beginTransaction()
                .replace(R.id.fileListContainer, fileListFragment, dir.absolutePath)
                .apply { oldDir?.let { addToBackStack(it.absolutePath) } }
                .commitAllowingStateLoss()
    }

    override fun onBackPressed(): Boolean {
        val count = childFragmentManager.backStackEntryCount
        if (count > 0) {
            currentDir = File(childFragmentManager.getBackStackEntryAt(count - 1).name)
            childFragmentManager.popBackStackImmediate()
            updatePathIndicator()
            return true
        }
        return super.onBackPressed()
    }

}