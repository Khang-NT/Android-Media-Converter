package com.github.khangnt.mcp.util

import android.arch.lifecycle.MutableLiveData

/**
 * Created by Khang NT on 3/29/18.
 * Email: khang.neon.1997@gmail.com
 */

@Suppress("unused")
class DistinctLiveData<T>() : MutableLiveData<T>() {
    constructor(init: T): this() {
        setValue(init)
    }

    override fun setValue(value: T) {
        if (value != getValue()) {
            super.setValue(value)
        }
    }
}