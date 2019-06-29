package com.github.khangnt.mcp.util

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

//
//class DistinctLiveData<T> : MediatorLiveData<T>() {
//    override fun setValue(value: T) {
//        if (value != super.getValue()) {
//            super.setValue(value)
//        }
//    }
//}
//
//fun <T> LiveData<T>.distinct(): DistinctLiveData<T> {
//    val distinctLiveData = DistinctLiveData<T>()
//    distinctLiveData.addSource(this, { if (it != null) distinctLiveData.setValue(it) })
//    return distinctLiveData
//}
//
//

class LiveEvent : LiveData<Any>() {

    @MainThread
    fun fireEvent() {
        value = Any()
    }

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in Any>) {
        super.observe(owner, createObserverWrapper(observer))
    }

    @MainThread
    override fun observeForever(observer: Observer<in Any>) {
        super.observeForever(createObserverWrapper(observer))
    }

    @MainThread
    private fun createObserverWrapper(observer: Observer<Any>): Observer<Any> {
        var ignorePastEvent = value != null
        return Observer {
            if (!ignorePastEvent) {
                observer.onChanged(it)
            } else {
                ignorePastEvent = false
            }
        }
    }
}

//
//
//class DebounceLiveData<T>(private val delayMs: Long) : MediatorLiveData<T>() {
//    companion object {
//        private const val MSG_SET_VALUE = 0
//    }
//    private val mainHandler = Handler(Looper.getMainLooper(), Handler.Callback { msg: Message ->
//        if (msg.what == MSG_SET_VALUE) {
//            @Suppress("UNCHECKED_CAST")
//            super.setValue(msg.obj as T)
//        }
//        return@Callback true
//    })
//
//    override fun setValue(value: T) {
//        synchronized(mainHandler) {
//            mainHandler.removeMessages(MSG_SET_VALUE)
//            mainHandler.sendMessageDelayed(Message().apply {
//                what = MSG_SET_VALUE
//                obj = value
//            }, delayMs)
//        }
//
//    }
//
//    override fun postValue(value: T) {
//        // now it's safe to call setValue in background thread
//        setValue(value)
//    }
//
//}
//
//fun

