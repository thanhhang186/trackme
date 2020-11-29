package com.khtn.trackme.database

import android.os.Handler
import android.os.HandlerThread

/**
 * Created by NguyenHang on 11/28/2020.
 */

class HandlerWorkerThread (threadName: String) : HandlerThread(threadName) {

    private lateinit var workerHandler: Handler

    override fun onLooperPrepared() {
        super.onLooperPrepared()

        workerHandler = Handler(looper)
    }

    fun postTask(task: Runnable) {
        if (::workerHandler.isInitialized){
            workerHandler.post(task)
        }
    }
}