package com.github.khangnt.mcp.ui

import android.content.Intent
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

        if (supportFragmentManager.findFragmentByTag(CONTENT_FRAGMENT_TAG) == null) {
            val fragment = onCreateFragment(savedInstanceState)
            (fragment as? BaseFragment)?.setActivityIntent(intent)
            supportFragmentManager.beginTransaction()
                    .add(getFragmentContainerId(), fragment, CONTENT_FRAGMENT_TAG)
                    .commit()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        (getContentFragment() as? BaseFragment)?.onNewActivityIntent(intent)
    }

    /**
     * @return The fragment created in [onCreateFragment], if it is still attached
     * to this activity.
     */
    protected fun getContentFragment(): Fragment {
        return supportFragmentManager.findFragmentByTag(CONTENT_FRAGMENT_TAG)
    }

    /**
     * Creates the layout for this activity. The default implementation is an empty activity where
     * the fragment consumes the entire window.
     * @see [getFragmentContainerId]
     */
    protected fun onCreateLayout(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_single_fragment)
    }

    /**
     * @return The layout ID that the fragment for this activity should be attached to.
     */
    @IdRes
    protected fun getFragmentContainerId(): Int {
        return R.id.fragment_container
    }
}