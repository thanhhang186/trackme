package com.khtn.trackme.modules.base

/**
 * Created by NguyenHang on 11/25/2020.
 */

interface BaseContract {
    interface BaseView {
        fun showLoading()
        fun dismissLoading()
    }

    interface BasePresenter {
        fun onAttach(view: BaseView?)
        fun onDestroy()
    }

}