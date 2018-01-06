package com.github.khangnt.mcp.ui.job_manager

import android.annotation.SuppressLint
import android.view.View
import com.github.khangnt.mcp.ui.common.HeaderModel
import com.github.khangnt.mcp.ui.common.ItemHeaderViewHolder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

/**
 * Created by Khang NT on 1/5/18.
 * Email: khang.neon.1997@gmail.com
 */

class RunningHeaderModel(header: String) : HeaderModel(header)

@SuppressLint("SetTextI18n")
class ItemHeaderRunningViewHolder(
        itemView: View,
        outputSize: Observable<String>,
        compositeDisposable: CompositeDisposable
) : ItemHeaderViewHolder(itemView) {
    private var speedSuffix = ""
    private var header = ""

    init {
        val disposable = outputSize.throttleLast(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    speedSuffix = it
                    tvHeader.text = "$header $speedSuffix"
                }, {
                    speedSuffix = ""
                    tvHeader.text = header
                })
        compositeDisposable.add(disposable)
    }

    override fun bind(model: HeaderModel, pos: Int) {
        header = model.header
        tvHeader.text = "$header $speedSuffix"
    }
}