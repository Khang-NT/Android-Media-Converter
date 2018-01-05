package com.github.khangnt.mcp.ui

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent
import android.support.annotation.Keep
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber


/**
 * Created by Khang NT on 1/5/18.
 * Email: khang.neon.1997@gmail.com
 */

typealias FragmentEventCallback = (BaseFragment) -> Unit
typealias ActivityEventCallback = (BaseActivity) -> Unit

class LifecycleProvider(lifecycle: Lifecycle) : LifecycleObserver {
    private val subject = BehaviorSubject.create<Lifecycle.Event>()
    private val onDestroyViewListeners = mutableListOf<() -> Unit>()
    private var viewDestroyed = false

    init {
        lifecycle.addObserver(this)
    }

    fun getSubject(): BehaviorSubject<Lifecycle.Event> = subject

    @Keep
    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    fun onAny(source: LifecycleOwner, event: Lifecycle.Event) {
        subject.onNext(event)
    }

    fun runOnEvent(event: Lifecycle.Event, activity: BaseActivity, action: ActivityEventCallback) {
        subject.filter { it == event }.take(1).subscribe(
                { action.invoke(activity) },
                { Timber.d(it) }
        )
    }

    fun runOnEvent(event: Lifecycle.Event, fragment: BaseFragment, action: FragmentEventCallback) {
        subject.filter { it == event }.take(1).subscribe(
                { action.invoke(fragment) },
                { Timber.d(it) }
        )
    }


    // special for Fragments

    fun runOnDestroyView(action: () -> Unit) {
        synchronized(onDestroyViewListeners) {
            onDestroyViewListeners.add(action)
            if (viewDestroyed) {
                // invoke callbacks immediately
                callbackOnDestroyViewListeners()
            }
        }
    }

    fun notifyOnDestroyView() {
        synchronized(onDestroyViewListeners) {
            viewDestroyed = true
            callbackOnDestroyViewListeners()
        }
    }

    fun notifyOnViewCreate() {
        synchronized(onDestroyViewListeners) {
            viewDestroyed = false
        }
    }

    private fun callbackOnDestroyViewListeners() {
        onDestroyViewListeners.removeAll { listener ->
            listener.invoke()
            true
        }
    }

}