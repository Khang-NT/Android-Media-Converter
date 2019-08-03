package com.github.khangnt.mcp.ui

import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created by Khang NT on 1/31/18.
 * Email: khang.neon.1997@gmail.com
 */

open class RxAppCompatActivity : AppCompatActivity() {

    private var disposableOnPaused = CompositeDisposable()
    private val disposableOnDestroyed = CompositeDisposable()

    override fun onResume() {
        super.onResume()
        if (disposableOnPaused.isDisposed) disposableOnPaused = CompositeDisposable()
    }

    override fun onPause() {
        super.onPause()
        disposableOnPaused.dispose()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposableOnDestroyed.dispose()
    }

    protected fun Disposable.disposeOnPaused() = apply { disposableOnPaused.add(this) }

    protected fun Disposable.disposeOnDestroyed() = apply { disposableOnDestroyed.add(this) }

}