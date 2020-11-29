package com.khtn.trackme.services

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.khtn.trackme.setting.Constants


/**
 * Created by NguyenHang on 11/28/2020.
 */

class LocationMonitoringService : Service(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    LocationListener {
    companion object {
        const val ACTION_LOCATION_BROADCAST = "LocationBroadcast"
        const val EXTRA_LATITUDE = "extra_latitude"
        const val EXTRA_LONGITUDE = "extra_longitude"
    }

    var mLocationClient: GoogleApiClient? = null
    var mLocationRequest = LocationRequest()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mLocationClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()

        mLocationRequest.interval = Constants.LOCATION_INTERVAL
        mLocationRequest.fastestInterval = Constants.FASTEST_LOCATION_INTERVAL

        // Request the most accurate locations available
        val priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.priority = priority
        mLocationClient?.connect()

        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onConnected(p0: Bundle?) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, this)
    }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    override fun onLocationChanged(location: Location?) {
        if (location != null) {
            sendMessageToUI(location.latitude, location.longitude)
        }
    }

    private fun sendMessageToUI(lat: Double, lng: Double) {
        val intent = Intent(ACTION_LOCATION_BROADCAST)
        val bundle = Bundle()
        bundle.putDouble(EXTRA_LATITUDE, lat)
        bundle.putDouble(EXTRA_LONGITUDE, lng)
        intent.putExtras(bundle)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

}