package com.github.khangnt.mcp.ui

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.github.khangnt.mcp.SingletonInstances
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber


/**
 * Jockey clean UI architecture by Marverenic.
 * https://github.com/marverenic/Jockey/blob/master/app/src/main/java/com/marverenic/music/ui/BaseActivity.java
 */
abstract class BaseActivity : RxAppCompatActivity() {

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onBackPressed() {
        Timber.v("onBackPressed")

        val fragments: List<Fragment> = supportFragmentManager.fragments
        if (fragments.any { fragment -> fragment is BaseFragment && fragment.onBackPressed() }) {
            // BaseFragment consumed back press event
            return
        }

        if (!consumeBackPress()) {
            super.onBackPressed()
            finish()
        }
    }

    open fun consumeBackPress(): Boolean = false

    open fun getSnackBarContainer(): View? = null

    fun showSnackBar(message: String, container: View? = null) {
        (container ?: getSnackBarContainer())?.let { view ->
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    protected inline fun <reified T : ViewModel> getViewModel(key: String? = null): T {
        return ViewModelProviders.of(this, SingletonInstances.getViewModelFactory()).run {
            key?.let { get(it, T::class.java) } ?: get(T::class.java)
        }
    }

    protected fun <T> LiveData<T>.observe(action: (T) -> Unit) {
        observe(this@BaseActivity, Observer { it?.let(action) })
    }
}