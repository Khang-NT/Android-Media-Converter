package com.github.khangnt.mcp.ui

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v7.app.ActionBarDrawerToggle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.github.khangnt.mcp.R
import kotlinx.android.synthetic.main.fragment_main.*

/**
 * Created by Khang NT on 1/5/18.
 * Email: khang.neon.1997@gmail.com
 */

class MainFragment : BaseFragment(), NavigationView.OnNavigationItemSelectedListener {

    override fun createView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.let {
//            drawerToggleListener = ActionBarDrawerToggle(activity, drawerLayout, toolbar, R
//                    .string.open_drawer_des, R.string.close_drawer_des)
//            drawerLayout.addDrawerListener(drawerToggleListener)
            return@let inflater.inflate(R.layout.fragment_main, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigationView.setNavigationItemSelectedListener(this)
        setActivitySupportActionBar(toolbar)
        collapsingToolbar.title = "Preset command"
        val drawerToggleListener = ActionBarDrawerToggle(activity, drawerLayout, toolbar,
                R.string.open_drawer_des, R.string.close_drawer_des)
        drawerLayout.addDrawerListener(drawerToggleListener)
        drawerToggleListener.syncState()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return true
    }

}