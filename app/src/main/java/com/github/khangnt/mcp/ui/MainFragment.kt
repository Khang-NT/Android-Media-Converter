package com.github.khangnt.mcp.ui

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBarDrawerToggle
import android.view.*
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.job_manager.JobManagerFragment
import com.github.khangnt.mcp.ui.preset_cmd.PresetCommandFragment
import kotlinx.android.synthetic.main.fragment_main.*

/**
 * Created by Khang NT on 1/5/18.
 * Email: khang.neon.1997@gmail.com
 */

private const val PRESET_COMMAND_FRAG_TAG = "TAG:PresetFragment"
private const val JOB_MANAGER_FRAG_TAG = "TAG:JobManagerFragment"
private const val SETTING_FRAG_TAG = "TAG:SettingFragment"
private const val ABOUT_FRAG_TAG = "TAG:AboutFragment"

private const val KEY_BACK_STACK = "KEY:BackStack"
private const val KEY_CURRENT_FRAGMENT = "KEY:CurrentFragment"

class MainFragment : BaseFragment(), NavigationView.OnNavigationItemSelectedListener {

    private var currentFragment: String? = null
    private val backStack = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentFragment = JOB_MANAGER_FRAG_TAG
        savedInstanceState?.let {
            val temp = it.getStringArrayList(KEY_BACK_STACK) ?: emptyList<String>()
            backStack.clear()
            backStack.addAll(temp)

            currentFragment = it.getString(KEY_CURRENT_FRAGMENT, currentFragment)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_main, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigationView.setNavigationItemSelectedListener(this)
        setActivitySupportActionBar(toolbar)
        val drawerToggleListener = ActionBarDrawerToggle(activity, drawerLayout, toolbar,
                R.string.open_drawer_des, R.string.close_drawer_des)
        drawerLayout.addDrawerListener(drawerToggleListener)
        drawerToggleListener.syncState()

        // just ensure current fragment is showing, don't add it to back stack
        showFragment(currentFragment!!, false)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (!item.isChecked) {
            drawerLayout.closeDrawer(Gravity.START)
            val fragmentToShow = when (item.itemId) {
                R.id.item_nav_job_manager -> JOB_MANAGER_FRAG_TAG
                R.id.item_nav_preset_command -> PRESET_COMMAND_FRAG_TAG
                R.id.item_nav_setting -> SETTING_FRAG_TAG
                R.id.item_nav_about -> ABOUT_FRAG_TAG
                else -> null
            }
            if (fragmentToShow !== null) {
                showFragment(fragmentToShow, true)
            }
        }
        return true
    }

    override fun onBackPressed(): Boolean {
        return if (drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawer(Gravity.START)
            true
        } else if (!backStack.isEmpty()) {
            showFragment(backStack.removeAt(backStack.size - 1), false)
            true
        } else {
            false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(KEY_BACK_STACK, ArrayList(backStack))
        outState.putString(KEY_CURRENT_FRAGMENT, currentFragment)
    }

    private fun createFragmentInstance(fragmentTag: String): Fragment =
            when (fragmentTag) {
                PRESET_COMMAND_FRAG_TAG -> PresetCommandFragment()
                JOB_MANAGER_FRAG_TAG -> JobManagerFragment()
                SETTING_FRAG_TAG -> PresetCommandFragment()
                ABOUT_FRAG_TAG -> AboutFragment()
                else -> throw IllegalArgumentException("Unknown fragment tag: $fragmentTag")
            }

    private fun fragmentTitle(fragmentTag: String): String =
            when (fragmentTag) {
                PRESET_COMMAND_FRAG_TAG -> getString(R.string.nav_preset_commands)
                JOB_MANAGER_FRAG_TAG -> getString(R.string.nav_job_manager)
                SETTING_FRAG_TAG -> getString(R.string.nav_settings)
                ABOUT_FRAG_TAG -> getString(R.string.nav_about)
                else -> throw IllegalArgumentException("Unknown fragment tag: $fragmentTag")
            }

    private fun showFragment(fragmentTag: String, addToBackStack: Boolean) {
        val shouldAddToBackStack = addToBackStack &&
                currentFragment != SETTING_FRAG_TAG &&  // shouldn't back to setting
                currentFragment != ABOUT_FRAG_TAG       // or about page

        if (shouldAddToBackStack) {
            backStack.remove(currentFragment) // remove currentFragment in back stack if exists
            backStack.add(currentFragment!!) // add it to top
        }

        val fragment = childFragmentManager.findFragmentByTag(fragmentTag) ?:
                createFragmentInstance(fragmentTag)
        val showingFragment = childFragmentManager.findFragmentById(R.id.contentContainer)
        if (showingFragment !== fragment) {
            childFragmentManager.beginTransaction()
                    .replace(R.id.contentContainer, fragment, fragmentTag)
                    .commit()
        }

        currentFragment = fragmentTag
        collapsingToolbar.title = fragmentTitle(fragmentTag)
    }

}