package com.github.khangnt.mcp.ui.jobmaker.selectfile

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.common.MixAdapter
import com.github.khangnt.mcp.ui.jobmaker.JobMakerViewModel
import com.github.khangnt.mcp.ui.jobmaker.StepFragment
import com.github.khangnt.mcp.util.getViewModel
import com.github.khangnt.mcp.util.toast
import kotlinx.android.synthetic.main.fragment_selected_files.*

/**
 * Created by Khang NT on 4/6/18.
 * Email: khang.neon.1997@gmail.com
 */

class SelectedFilesFragment : StepFragment() {

    /** Get shared view model via host activity **/
    private val jobMakerViewModel by lazy { requireActivity().getViewModel<JobMakerViewModel>() }
    private val itemTouchHelper by lazy { ItemTouchHelper(makeItemTouchHelperCallback()) }
    private val adapter: MixAdapter by lazy {
        MixAdapter.Builder {
            withModel<FileModel> {
                ItemFileViewHolder.Factory {
                    onStartDrag = { itemTouchHelper.startDrag(it) }
                    onRemoveFile = { jobMakerViewModel.removeSelectedFiles(it) }
                }
            }
        }.build()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_selected_files, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.adapter = adapter
        itemTouchHelper.attachToRecyclerView(recyclerView)

        jobMakerViewModel.getSelectedFiles().observe { selectedFiles ->
            adapter.setData(selectedFiles.map { FileModel(it) })
            tvEmptyMessage.visibility = if (selectedFiles.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onGoToNextStep() {
        if (jobMakerViewModel.getSelectedFiles().value.orEmpty().isEmpty()) {
            toast(R.string.empty_selected_files)
        } else {
            jobMakerViewModel.setCurrentStep(JobMakerViewModel.STEP_CHOOSE_COMMAND)
        }
    }

    private fun makeItemTouchHelperCallback() = object : ItemTouchHelper.Callback() {

        private var dragFrom = -1
        private var dragTo = -1

        override fun isItemViewSwipeEnabled(): Boolean = false

        override fun isLongPressDragEnabled(): Boolean = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit

        override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
        ): Int {
            return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0)
        }

        override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
        ): Boolean {
            if (dragFrom == -1) {
                dragFrom = viewHolder.adapterPosition
            }
            dragTo = target.adapterPosition
            adapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            // update selected files
            if (dragFrom != -1 && dragTo != -1 && dragFrom != dragTo) {
                jobMakerViewModel.moveSelectedFiles(dragFrom, dragTo)
            }
            dragFrom = -1
            dragTo = -1
        }

    }

}