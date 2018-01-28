package com.github.khangnt.mcp.ui.presetcmd

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.BaseFragment
import kotlinx.android.synthetic.main.fragment_job_manager.*

/**
 * Created by Khang NT on 1/6/18.
 * Email: khang.neon.1997@gmail.com
 */

class PresetCommandFragment: BaseFragment() {
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_preset_command, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewGroup.recyclerView?.layoutManager = LinearLayoutManager(view.context)
        recyclerViewGroup.recyclerView?.adapter = emptyAdapter
        recyclerViewGroup.empty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerViewGroup.recyclerView?.adapter = null
    }
}

private val emptyAdapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        TODO("not implemented")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        TODO("not implemented")
    }

    override fun getItemCount(): Int = 0
}