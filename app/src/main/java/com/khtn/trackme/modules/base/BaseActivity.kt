package com.khtn.trackme.modules.base

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.khtn.trackme.modules.dialog.LoadingDialogFragment

/**
 * Created by NguyenHang on 11/26/2020.
 */

abstract class BaseActivity : AppCompatActivity(), BaseContract.BaseView {
    var loadingDialog: LoadingDialogFragment? = null

    override fun showLoading() {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialogFragment()
            supportFragmentManager.beginTransaction().add(loadingDialog as Fragment, LoadingDialogFragment::class.java.simpleName)
        }
    }

    override fun dismissLoading() {
        loadingDialog?.dismissAllowingStateLoss()
    }
}