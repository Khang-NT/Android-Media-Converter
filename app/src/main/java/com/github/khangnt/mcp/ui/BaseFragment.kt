package com.github.khangnt.mcp.ui

import android.app.Activity
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import timber.log.Timber


/**
 * Jockey clean UI architecture by Marverenic.
 * https://github.com/marverenic/Jockey/blob/master/app/src/main/java/com/marverenic/music/ui/BaseFragment.java
 */

abstract class BaseFragment : RxFragment() {

    /**
     * If this fragment is attached to a [BaseActivity], then this callback will be triggered
     * when the back button is pressed. If multiple BaseFragments are visible in a single activity,
     * there is no guarantee on which fragment will get the callback first.
     * @return Whether or not this event was consumed. If false, the activity will handle the event.
     * @see Activity.onBackPressed
     */
    open fun onBackPressed(): Boolean {
        return false
    }

    protected fun setActivitySupportActionBar(toolbar: Toolbar) {
        val hostingActivity = activity
        if (hostingActivity is AppCompatActivity) {
            hostingActivity.setSupportActionBar(toolbar)
        } else {
            Timber.w("Hosting activity is not an AppCompatActivity. Toolbar will not be bound.")
        }
    }

    protected fun getActivitySupportActionBar(): ActionBar? {
        return (activity as? AppCompatActivity)?.supportActionBar
    }

    protected fun <T> LiveData<T>.observe(action: (T) -> Unit) {
        observe(this@BaseFragment, Observer { it?.let(action) })
    }

}