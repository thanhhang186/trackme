package com.khtn.trackme.modules.main

import android.content.Context
import com.khtn.trackme.model.Track
import com.khtn.trackme.modules.base.BaseContract

/**
 * Created by NguyenHang on 11/24/2020.
 */
interface HistoryContract {
    interface View: BaseContract.BaseView, android.view.View.OnClickListener {
        fun updateTrackHistory(history: ArrayList<Track>)
        fun getView(): Context
    }

    interface Presenter: BaseContract.BasePresenter {
        fun loadTrackHistory()
    }
}