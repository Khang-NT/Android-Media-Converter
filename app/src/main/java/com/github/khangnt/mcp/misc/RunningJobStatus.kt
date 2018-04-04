package com.github.khangnt.mcp.misc

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject


object RunningJobStatus {
    private val subject = BehaviorSubject.create<String>()

    fun postUpdate(status: String) = subject.onNext(status)

    fun observeRunningJobStatus(): Observable<String> = subject
}