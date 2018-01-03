package com.github.khangnt.mcp

import android.app.Application

/**
 * Created by Khang NT on 1/2/18.
 * Email: khang.neon.1997@gmail.com
 */

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        SingletonInstances.init(this)
    }

}