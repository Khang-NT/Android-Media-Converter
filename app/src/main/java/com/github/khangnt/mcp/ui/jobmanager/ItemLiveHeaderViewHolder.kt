package com.github.khangnt.mcp.ui.jobmanager

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.common.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.item_header.view.*
import timber.log.Timber

/**
 * Created by Khang NT on 1/5/18.
 * Email: khang.neon.1997@gmail.com
 */

data class LiveHeaderModel(
        val headerFormat: String,
        val liveTextObservable: Observable<String>
) : AdapterModel, HasIdLong {
    override val idLong: Long by lazy { IdGenerator.idFor(headerFormat) }
}

@SuppressLint("SetTextI18n")
class ItemLiveHeaderViewHolder(itemView: View) : CustomViewHolder<LiveHeaderModel>(itemView) {
    private val tvHeader: TextView = itemView.tvHeader
    private var disposable: Disposable? = null

    override fun bind(model: LiveHeaderModel, pos: Int) {
        disposable?.dispose()
        disposable = model.liveTextObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    tvHeader.text = String.format(model.headerFormat, it)
                }, {
                    Timber.d(it)
                    tvHeader.text = ""
                })
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        disposable?.dispose()
    }



    class Factory : ViewHolderFactory {
        override val layoutRes: Int = R.layout.item_header

        override fun create(itemView: View): CustomViewHolder<*> {
            return ItemLiveHeaderViewHolder(itemView)
        }
    }
}