package com.github.khangnt.mcp.ui

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.View
import com.github.khangnt.mcp.SingletonInstances
import timber.log.Timber
import java.util.Collections.emptyList


/**
 * Jockey clean UI architecture by Marverenic.
 * https://github.com/marverenic/Jockey/blob/master/app/src/main/java/com/marverenic/music/ui/BaseActivity.java
 */
abstract class BaseActivity: RxAppCompatActivity() {

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onBackPressed() {
        Timber.v("onBackPressed")

        val fragments: List<Fragment> = supportFragmentManager.fragments ?: emptyList()
        if (fragments.any { fragment -> fragment is BaseFragment && fragment.onBackPressed()}) {
            // BaseFragment consumed back press event
            return
        }

        super.onBackPressed()
        finish()
    }

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

}