package com.khtn.trackme.modules.record

import android.Manifest
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import com.khtn.trackme.BuildConfig
import com.khtn.trackme.R
import com.khtn.trackme.database.AppDatabase
import com.khtn.trackme.database.HandlerWorkerThread
import com.khtn.trackme.model.Track
import com.khtn.trackme.modules.base.BaseActivity
import com.khtn.trackme.services.LocationMonitoringService
import com.khtn.trackme.setting.Constants
import com.khtn.trackme.utils.Utils
import kotlinx.android.synthetic.main.activity_record.*


class RecordActivity : BaseActivity(), View.OnClickListener, OnMapReadyCallback {
    companion object {
        private const val REQUEST_PERMISSION_LOCATION_CODE = 1125
        private const val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
    }

    private var isAlreadyStartedService = false
    private val TAG = "main"
    private var isRecord = true
    private val locations = ArrayList<com.khtn.trackme.model.Location>()
    private var currentTrack: Track? = null
    private var distance = 0.0
    private var speed = 0.0
    private var counterTrackingLocation = 0
    private var avgSpeed = 0.0
    private var startTime = 0L
    private var currentDuration = 0L
    private var previousDuration = 0L
    private var durationHandler: Handler? = null
    private var isShowSnackBar = false

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var googleMap: GoogleMap? = null
    private var markerCurrent: Marker? = null

    private var appDb: AppDatabase? = null
    private var dbWorkerThread: HandlerWorkerThread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)
        appDb = AppDatabase.getInstance(this)
        dbWorkerThread = HandlerWorkerThread("dbWorkThread")
        if (dbWorkerThread?.isAlive == false) {
            dbWorkerThread?.start()
        }
        registerLocationMonitoringService()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        initView()
    }

    override fun onResume() {
        super.onResume()
        checkGooglePlayService()
    }

    private fun initView() {
        btn_replay?.setOnClickListener(this)
        btn_stop?.setOnClickListener(this)
        btn_pause?.setOnClickListener(this)
        showReplayStopButton(false)
        tv_distance.text = getString(R.string.distance, "--")
        tv_speed.text = getString(R.string.speed, "--")
        tv_duration.text = "--"
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    private fun registerLocationMonitoringService() {
        val intentFilter = IntentFilter(LocationMonitoringService.ACTION_LOCATION_BROADCAST)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastLocationReceiver, intentFilter)
    }

    private val broadcastLocationReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            if (isRecord) {
                val bundle = intent?.extras
                val latitude = bundle?.getDouble(LocationMonitoringService.EXTRA_LATITUDE, 0.0)?: 0.0
                val longitude = bundle?.getDouble(LocationMonitoringService.EXTRA_LONGITUDE, 0.0)?: 0.0
                onLocationChanged(latitude, longitude)
            }
        }
    }

    /**
     * Step 1: Check Google Play services
     */
    private fun checkGooglePlayService() {
        // Check whether this user has installed Google play service which is being used by Location updates.
        if (isGooglePlayServicesAvailable()) {
            //Passing null to indicate that it is executing for the first time.
            checkNetworkConnection(null)
        } else {
            Toast.makeText(this, R.string.no_google_play_service_available, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Step 2: Check & Prompt Internet connection
     */
    private fun checkNetworkConnection(dialog: DialogInterface?): Boolean? {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected) {
            promptInternetConnect()
            return false
        }
        dialog?.dismiss()

        //Yes there is active internet connection. Next check Location is granted by user or not.
        if (checkLocationPermissions()) {
            //Yes permissions are granted by the user. Go to the next step.
            getLocationWithGps()
        } else {
            //No user has not granted the permissions yet. Request now.
            requestPermissions()
        }
        return true
    }

    /**
     * Step 3: Start the Location Monitor Service
     */
    private fun getLocationWithGps() {
        // And it will be keep running until you close the entire application from task manager.
        // This method will executed only once.
        if (!isAlreadyStartedService) {
            //Start location sharing service to app server.........
            val intent = Intent(this, LocationMonitoringService::class.java)
            startService(intent)
            isAlreadyStartedService = true

            clearData()
            startTime = System.currentTimeMillis()
            updateDuration()
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btn_pause -> {
                showReplayStopButton(true)
                previousDuration = currentDuration
                isRecord = false
                durationHandler?.removeCallbacks(durationRunnable)
                showReplayStopButton(true)
            }
            R.id.btn_replay -> {
                showReplayStopButton(false)
                isRecord = true
                currentDuration = 0
                startTime = System.currentTimeMillis()
                updateDuration()
            }
            R.id.btn_stop -> {
                // Save record -> go to history view
                storeTrack()
            }
        }
    }

    private fun checkLocationPermissions(): Boolean {
        val hasFineLocation = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasBackgroundLocation = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return hasFineLocation && hasCoarseLocation && hasBackgroundLocation
    }

    /**
     * Return the availability of GooglePlayServices
     */
    private fun isGooglePlayServicesAvailable(): Boolean {
        val googleApi = GoogleApiAvailability.getInstance()
        val status = googleApi.isGooglePlayServicesAvailable(this)
        if (status != ConnectionResult.SUCCESS) {
            if (googleApi.isUserResolvableError(status)) {
                googleApi.getErrorDialog(this, status, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false
        }
        return true
    }

    /**
     * Show A Dialog with button to refresh the internet state.
     */
    private fun promptInternetConnect() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.title_alert_no_internet)
        builder.setMessage(R.string.msg_alert_no_internet)
        val positiveText = getString(R.string.btn_label_refresh)
        builder.setPositiveButton(positiveText,
            DialogInterface.OnClickListener { dialog, which ->
                //Block the Application Execution until user grants the permissions
                checkNetworkConnection(dialog)
            })
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showReplayStopButton(isShow: Boolean) {
        if (isShow) {
            btn_pause?.visibility = View.INVISIBLE
            btn_replay?.visibility = View.VISIBLE
            btn_stop?.visibility = View.VISIBLE
        } else {
            btn_pause?.visibility = View.VISIBLE
            btn_replay?.visibility = View.INVISIBLE
            btn_stop?.visibility = View.INVISIBLE
        }
    }

    private fun requestPermissions() {
        val shouldProvideRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

        val shouldProvideRationale2 =
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

        val shouldProvideRationale3 =
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        if (shouldProvideRationale || shouldProvideRationale2 || shouldProvideRationale3) {
            showSnackBar(R.string.permission_rationale, android.R.string.ok,
                View.OnClickListener { v: View? ->
                    isShowSnackBar = false
                    ActivityCompat.requestPermissions(
                        this@RecordActivity,
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ),
                        REQUEST_PERMISSION_LOCATION_CODE
                    )
                }
            )
        } else {
            ActivityCompat.requestPermissions(
                this@RecordActivity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                REQUEST_PERMISSION_LOCATION_CODE
            )
        }

    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION_LOCATION_CODE) {
            if (grantResults.isEmpty()) {
                // If img_user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationWithGps()
            } else {
                // Permission denied.
                // Build intent that displays the App settings screen.
                showSnackBar(R.string.permission_denied_explanation,
                    R.string.settings,
                    View.OnClickListener {
                        isShowSnackBar = false
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts(
                            "package",
                            BuildConfig.APPLICATION_ID, null
                        )
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    })
            }
        }
    }

    private fun showSnackBar(mainTextStringId: Int, actionStringId: Int, listener: View.OnClickListener) {
        if (!isShowSnackBar) {
            Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE
            ).setAction(getString(actionStringId), listener).show()
            isShowSnackBar = true
        }
    }

    private fun onLocationChanged(latitude: Double, longitude: Double) {
        counterTrackingLocation++
        if (currentTrack == null) {
            this.currentTrack = Track()
            this.currentTrack?.id = System.currentTimeMillis()
        }
        val location = com.khtn.trackme.model.Location(currentTrack?.id?: 0, latitude, longitude)
        locations.add(location)

        val latLng = LatLng(location.latitude ?: 0.0, location.longitude ?: 0.0)
        if (markerCurrent == null) {
            googleMap?.addMarker(MarkerOptions().position(latLng))
            val currentMarker = MarkerOptions()
            currentMarker.position(latLng)
            currentMarker.icon(Utils.bitmapDescriptorFromVector(this, R.drawable.ic_my_location))
            markerCurrent = googleMap?.addMarker(currentMarker)
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
        }

        if (locations.size >= 2) {
            val polylineOptions = PolylineOptions()
            // Get previous location
            val locationBegin = locations[locations.size - 2]
            val lastLatlng = LatLng(locationBegin.latitude ?: 0.0, locationBegin.longitude ?: 0.0)
            polylineOptions.add(lastLatlng)
            polylineOptions.add(latLng)
            polylineOptions.width(5f).color(Color.BLUE)
            googleMap?.addPolyline(polylineOptions)
            markerCurrent?.position = latLng

            val startPoint = Location("startPoint")
            startPoint.latitude = lastLatlng.latitude
            startPoint.longitude = lastLatlng.longitude

            val currentPoint = Location("currentPoint")
            currentPoint.latitude = latLng.latitude
            currentPoint.longitude = latLng.longitude

            val distanceBetween2Track = (startPoint.distanceTo(currentPoint) / 1000).toDouble()
            distance += distanceBetween2Track
            speed = (distanceBetween2Track / Constants.LOCATION_INTERVAL) * 1000
            avgSpeed = (avgSpeed * (counterTrackingLocation - 1) + speed) / counterTrackingLocation
        }
        updateInfo()
    }

    override fun onMapReady(map: GoogleMap?) {
        googleMap = map
    }

    override fun onDestroy() {
        stopService(Intent(this, LocationMonitoringService::class.java))
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastLocationReceiver)
        isAlreadyStartedService = false
        clearData()
        dbWorkerThread?.looper?.quit()
        dbWorkerThread?.quit()
        dbWorkerThread = null
        super.onDestroy()
    }

    private fun clearData() {
        distance = 0.0
        speed = 0.0
        avgSpeed = 0.0
        startTime = 0L
        currentDuration = 0L
        previousDuration = 0L
        durationHandler?.removeCallbacks(durationRunnable)
        locations.clear()
        currentTrack = null
    }

    private fun updateInfo() {
        tv_distance.text = getString(R.string.distance, Utils.formatDouble(distance))
        tv_speed.text = getString(R.string.speed, Utils.formatDouble(speed))
    }

    private fun updateDuration() {
        durationHandler = Handler()
        durationHandler?.post(durationRunnable)
    }

    private val durationRunnable = object : Runnable {
        override fun run() {
            if (isRecord) {
                currentDuration = System.currentTimeMillis() - startTime + previousDuration
                tv_duration.text = Utils.formatDuration(currentDuration / 1000L)
                durationHandler?.post(this)
            }
        }
    }

    private fun storeTrack() {
        val task = Runnable {
            if (currentTrack == null) {
                this.currentTrack = Track()
                this.currentTrack?.id = System.currentTimeMillis()
            }
            this.currentTrack?.duration = currentDuration / 1000
            this.currentTrack?.distance = distance
            this.currentTrack?.averageSpeed = avgSpeed
            this.currentTrack?.locations = locations

            if (currentTrack != null) {
                appDb?.trackDao()?.insertTrack(currentTrack!!)
                locations.forEach {location ->
                    appDb?.locationDao()?.insertLocation(location)
                }
                finish()
            }
        }

        dbWorkerThread?.postTask(task)
    }
}