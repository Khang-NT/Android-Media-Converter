package com.github.khangnt.mcp.ui

import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import com.github.khangnt.mcp.R


/**
 * Jockey clean UI architecture by Marverenic.
 * https://github.com/marverenic/Jockey/blob/master/app/src/main/java/com/marverenic/music/ui/SingleFragmentActivity.java
 */

abstract class SingleFragmentActivity : BaseActivity() {
    companion object {
        private const val CONTENT_FRAGMENT_TAG = "content_fragment"
    }

    protected abstract fun onCreateFragment(savedInstanceState: Bundle?): Fragment

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onCreateLayout(savedInstanceState)

        var fragment = getContentFragment()
        if (fragment === null) {
            fragment = onCreateFragment(savedInstanceState)
            replaceFragment(fragment)
        }

        onFragmentCreated(fragment, savedInstanceState)
    }

    protected open fun onFragmentCreated(fragment: Fragment, savedInstanceState: Bundle?) {
        // No op
    }

    protected open fun replaceFragment(newFragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(getFragmentContainerId(), newFragment, CONTENT_FRAGMENT_TAG)
                .commitAllowingStateLoss()
    }

    /**
     * @return The fragment created in [onCreateFragment], if it is still attached
     * to this activity.
     */
    protected open fun getContentFragment(): Fragment? {
        return supportFragmentManager.findFragmentByTag(CONTENT_FRAGMENT_TAG)
    }

    /**
     * Creates the layout for this activity. The default implementation is an empty activity where
     * the fragment consumes the entire window.
     * @see [getFragmentContainerId]
     */
    protected open fun onCreateLayout(savedInstanceState: Bundle?) {
        setContentView(R.layout.single_fragment)
    }

    /**
     * @return The layout ID that the fragment for this activity should be attached to.
     */
    @IdRes
    protected open fun getFragmentContainerId(): Int {
        return R.id.fragmentContainer
    }
}