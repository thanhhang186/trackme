package com.khtn.trackme.modules.main

import com.khtn.trackme.database.AppDatabase
import com.khtn.trackme.database.HandlerWorkerThread
import com.khtn.trackme.model.Location
import com.khtn.trackme.model.Track
import com.khtn.trackme.modules.base.BaseContract

/**
 * Created by NguyenHang on 11/24/2020.
 */
class HistoryPresenter: HistoryContract.Presenter {
    private var view: HistoryContract.View? = null
    private var appDb: AppDatabase? = null
    private var dbWorkerThread: HandlerWorkerThread? = null

    override fun onAttach(view: BaseContract.BaseView?) {
        this.view = view as? HistoryContract.View
        if (this.view?.getView() != null) {
            appDb = AppDatabase.getInstance(this.view?.getView()!!)
        }
        dbWorkerThread = HandlerWorkerThread("getTracksWorkThread")
        if (dbWorkerThread?.isAlive == false) {
            dbWorkerThread?.start()
        }
    }

    override fun loadTrackHistory() {
        view?.showLoading()
        val task = Runnable {
            view?.dismissLoading()
            val tracks =  appDb?.trackDao()?.getAllTracks()
            tracks?.forEach { track ->
                track.locations = appDb?.locationDao()?.getLocationByTrackId(track.id) as ArrayList<Location>
            }
            view?.updateTrackHistory(tracks as ArrayList<Track>)
        }

        dbWorkerThread?.postTask(task)
    }

    override fun onDestroy() {
        view = null
        dbWorkerThread?.looper?.quit()
        dbWorkerThread?.quit()
        dbWorkerThread = null
    }
}