package com.khtn.trackme.modules.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.khtn.trackme.R
import com.khtn.trackme.adapter.TrackHistoryAdapter
import com.khtn.trackme.model.Track
import com.khtn.trackme.modules.base.BaseActivity
import com.khtn.trackme.modules.record.RecordActivity
import kotlinx.android.synthetic.main.activity_history.*

class HistoryActivity : BaseActivity(), HistoryContract.View {
    private var historyAdapter: TrackHistoryAdapter? = null
    private var tracks = ArrayList<Track>()
    private var presenter: HistoryPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        presenter = HistoryPresenter()
        presenter?.onAttach(this)
        initView()
    }

    private fun initView() {
        tv_default_history?.visibility = View.GONE
        rv_track_list?.visibility = View.VISIBLE
        btn_record?.setOnClickListener(this)
        rv_track_list?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        historyAdapter = TrackHistoryAdapter(this, tracks)
        rv_track_list?.adapter = historyAdapter
    }

    override fun onResume() {
        super.onResume()
        presenter?.loadTrackHistory()
        val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)

        if (resultCode == ConnectionResult.SUCCESS) {
            historyAdapter?.notifyDataSetChanged()
        } else {
            GoogleApiAvailability.getInstance().getErrorDialog(this, resultCode, 1).show()
        }
    }

    override fun updateTrackHistory(history: ArrayList<Track>) {
        runOnUiThread {
            if (history.isEmpty()) {
                tv_default_history?.visibility = View.VISIBLE
                rv_track_list?.visibility = View.GONE
            } else {
                tv_default_history?.visibility = View.GONE
                rv_track_list?.visibility = View.VISIBLE
                tracks.clear()
                tracks.addAll(history)
                historyAdapter?.setTracks(tracks)
                historyAdapter?.notifyDataSetChanged()
            }
        }
    }

    override fun onClick(view: View?) {
        if (view?.id == R.id.btn_record) {
            navigateTrackView()
        }
    }

    private fun navigateTrackView() {
        val intent = Intent(this, RecordActivity::class.java)
        startActivity(intent)
    }

    override fun getView(): Context {
        return this
    }

    override fun onDestroy() {
        presenter?.onDestroy()
        super.onDestroy()
    }
}