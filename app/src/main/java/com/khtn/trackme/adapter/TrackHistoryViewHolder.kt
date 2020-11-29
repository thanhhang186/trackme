package com.khtn.trackme.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.khtn.trackme.R
import com.khtn.trackme.model.Location
import com.khtn.trackme.model.Track
import com.khtn.trackme.utils.Utils
import kotlinx.android.synthetic.main.history_item.view.*


/**
 * Created by NguyenHang on 11/25/2020.
 */

class TrackHistoryViewHolder (private val context: Context?, itemView: View) : RecyclerView.ViewHolder(itemView), OnMapReadyCallback {
    private var googleMap: GoogleMap? = null
    private var locations: ArrayList<Location>? = null

    fun bind(track: Track?) {
        itemView.tv_distance?.text = context?.getString(R.string.distance, Utils.formatDouble(track?.distance?: 0.0))
        itemView.tv_avg_speed?.text = context?.getString(R.string.avg_speed, Utils.formatDouble(track?.averageSpeed?: 0.0))
        itemView.tv_duration?.text = Utils.formatDuration(track?.duration?: 0)

        locations = track?.locations
        itemView.map_view_track?.onCreate(null)
        itemView.map_view_track?.getMapAsync(this)
        if (googleMap != null) {
            updateMapContents()
        }
    }

    private fun getPolylineOption(locations: List<Location>?): PolylineOptions {
        val result = PolylineOptions()
        result.width(5f).color(Color.BLUE).geodesic(true)
        locations?.forEach {
            if (it.latitude != null && it.longitude != null) {
                val latlng = LatLng(it.latitude?: 0.0, it.longitude?: 0.0)
                result.add(latlng)
            }
        }
        return result
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        this.googleMap = googleMap
        MapsInitializer.initialize(context)
        googleMap?.uiSettings?.isMapToolbarEnabled = true
        if (locations?.isEmpty() == false) {
            updateMapContents()
        }
    }

    private fun updateMapContents() {
        // Clear pre-existing mapView
        googleMap?.clear()

        // Add polylines to the map to connect between points.
        googleMap?.addPolyline(getPolylineOption(locations))

        if (locations?.isEmpty() == false) {
            val destination = LatLng(locations?.last()?.latitude?: 0.0, locations?.last()?.longitude?: 0.0)
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(destination, 17f))

            val startLocation = LatLng(locations?.first()?.latitude?: 0.0, locations?.first()?.longitude?: 0.0)
            googleMap?.addMarker(MarkerOptions().position(startLocation))
            context?.let {
                googleMap?.addMarker(MarkerOptions().position(LatLng(destination.latitude, destination.longitude)).icon(Utils.bitmapDescriptorFromVector(context, R.drawable.ic_dot)))
            }
        }

    }
}