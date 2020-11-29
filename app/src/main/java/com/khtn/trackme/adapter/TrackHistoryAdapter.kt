package com.khtn.trackme.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.khtn.trackme.R
import com.khtn.trackme.model.Track

/**
 * Created by NguyenHang on 11/25/2020.
 */
class TrackHistoryAdapter :
    RecyclerView.Adapter<TrackHistoryViewHolder> {
    private var context: Context? = null
    private var trackList: ArrayList<Track>? = null

    constructor(context: Context?, trackList: ArrayList<Track>?) {
        this.context = context
        this.trackList = trackList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_item, parent, false)
        return TrackHistoryViewHolder(context, view)
    }

    override fun getItemCount(): Int {
        return trackList?.size ?: 0
    }

    fun setTracks(trackList: ArrayList<Track>?) {
        this.trackList = trackList
    }

    override fun onBindViewHolder(holder: TrackHistoryViewHolder, position: Int) {
        holder.bind(trackList?.get(position))
    }
}