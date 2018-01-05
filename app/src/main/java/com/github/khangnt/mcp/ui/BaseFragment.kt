package com.github.khangnt.mcp.ui

import android.app.Activity
import android.arch.lifecycle.Lifecycle
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import timber.log.Timber


/**
 * Jockey clean UI architecture by Marverenic.
 * https://github.com/marverenic/Jockey/blob/master/app/src/main/java/com/marverenic/music/ui/BaseFragment.java
 */

abstract class BaseFragment : Fragment() {
    protected val lifecycleProvider = LifecycleProvider(lifecycle)

    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        lifecycleProvider.notifyOnViewCreate()
        return createView(inflater, container, savedInstanceState)
    }

    open fun createView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = null

    /**
     * If this fragment is attached to a [BaseActivity], then this callback will be triggered
     * when the back button is pressed. If multiple BaseFragments are visible in a single activity,
     * there is no guarantee on which fragment will get the callback first.
     * @return Whether or not this event was consumed. If false, the activity will handle the event.
     * @see Activity.onBackPressed
     */
    fun onBackPressed(): Boolean {
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
        val hostingActivity = activity
        return if (hostingActivity is AppCompatActivity) {
            hostingActivity.supportActionBar
        } else {
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        lifecycleProvider.notifyOnDestroyView()
    }

    fun runOnDestroy(action: FragmentEventCallback) {
        lifecycleProvider.runOnEvent(Lifecycle.Event.ON_DESTROY, this, action)
    }

    fun runOnDestroyView(action: FragmentEventCallback) {
        lifecycleProvider.runOnDestroyView { action.invoke(this) }
    }

}