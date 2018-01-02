package com.github.khangnt.mcp

import android.annotation.SuppressLint
import android.app.Application

/**
 * Created by Khang NT on 1/2/18.
 * Email: khang.neon.1997@gmail.com
 */

class MainApplication: Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var singletonInstances: SingletonInstances
    }

    override fun onCreate() {
        super.onCreate()

        singletonInstances = SingletonInstances(this)
    }

}