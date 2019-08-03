package com.github.khangnt.mcp.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created by Khang NT on 1/30/18.
 * Email: khang.neon.1997@gmail.com
 */

open class RxFragment : Fragment() {
    private var disposableOnPaused: CompositeDisposable = CompositeDisposable()
    private var disposableOnDestroyed: CompositeDisposable = CompositeDisposable()
    private var disposableOnViewDestroyed: CompositeDisposable = CompositeDisposable()

    private var disposableMap = mutableMapOf<String, Disposable>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // renew disposable when fragment return from back stack
        if (disposableOnViewDestroyed.isDisposed) disposableOnViewDestroyed = CompositeDisposable()
    }

    override fun onResume() {
        super.onResume()
        // renew disposable when fragment resume
        if (disposableOnPaused.isDisposed) disposableOnPaused = CompositeDisposable()
    }

    protected fun Disposable.disposeOnPaused(tag: String? = null) = apply {
        tag?.let { disposableMap.put(it, this) }?.dispose()
        disposableOnPaused.add(this)
    }

    protected fun Disposable.disposeOnDestroyed(tag: String? = null) = apply {
        tag?.let { disposableMap.put(it, this) }?.dispose()
        disposableOnDestroyed.add(this)
    }

    protected fun Disposable.disposeOnViewDestroyed(tag: String? = null) = apply {
        tag?.let { disposableMap.put(it, this) }?.dispose()
        disposableOnViewDestroyed.add(this)
    }

    protected fun disposeTag(tag: String) = disposableMap[tag]?.dispose()

    override fun onPause() {
        super.onPause()
        disposableOnPaused.dispose()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposableOnViewDestroyed.dispose()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposableOnDestroyed.dispose()
    }
}