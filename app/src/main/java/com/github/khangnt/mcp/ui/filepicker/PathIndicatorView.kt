package com.github.khangnt.mcp.ui.filepicker

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.khangnt.mcp.ui.common.MixAdapter
import timber.log.Timber
import java.io.File

/**
 * Created by Khang NT on 2/2/18.
 * Email: khang.neon.1997@gmail.com
 */

class PathIndicatorView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    lateinit var onPathClick: (PathIndicatorView, File) -> Unit

    private var currentPath: File? = null

    private val pathIndicatorAdapter: MixAdapter = MixAdapter.Builder {
        withModel<PathIndicatorModel> {
            PathIndicatorViewHolder.Factory {
                onClick = {
                    onPathClick.invoke(this@PathIndicatorView, it.path)
                }
            }
        }
    }.build()

    init {
        super.setAdapter(pathIndicatorAdapter)
        super.setLayoutManager(LinearLayoutManager(context, HORIZONTAL, false))

        if (isInEditMode) {
            setPath(File("/storage/emulated/0/Hello"))
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        Timber.w("PathIndicatorView doesn't allow to set custom adapter")
    }

    override fun setLayoutManager(layout: LayoutManager?) {
        Timber.w("PathIndicatorView doesn't allow to set custom layout manager")
    }

    fun setPath(path: File) {
        if (currentPath != path) {
            currentPath = path
            val list = mutableListOf<PathIndicatorModel>()
            var temp: File? = path
            while (temp != null) {
                list.add(PathIndicatorModel(temp))
                temp = temp.parentFile
            }
            pathIndicatorAdapter.setData(list.reversed())
            postDelayed({ smoothScrollToPosition(pathIndicatorAdapter.itemCount) }, 200)
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val state = Bundle()
        state.putParcelable("super", super.onSaveInstanceState())
        state.putString("path", currentPath?.absolutePath)
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is Bundle) {
            super.onRestoreInstanceState(state)
        } else {
            super.onRestoreInstanceState(state.getParcelable("super"))
            state.getString("path")?.let { setPath(File(it)) }
        }
    }

}