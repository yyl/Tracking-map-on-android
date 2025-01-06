package com.yyl.myrmex.tracking

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Criteria
import android.location.LocationManager
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Process
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.yyl.myrmex.tracking.R

class MyService : Service() {

    private lateinit var mNM: NotificationManager
    private lateinit var mServiceLooper: Looper
    private lateinit var mServiceHandler: ServiceHandler
    private var NOTIFICATION = R.string.local_service_started
    private var last_time: Long = 0
    private lateinit var mgr: LocationManager
    private lateinit var gps_locationer: Locationer
    private lateinit var network_locationer: Locationer
    private lateinit var builder: NotificationCompat.Builder
    private val mu = MyUtility()
//    private lateinit var gpslistener: gpsStatusListener

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            mgr = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            gps_locationer = Locationer(baseContext)
            network_locationer = Locationer(baseContext)
//            gpslistener = gpsStatusListener()
//            mgr.addGpsStatusListener(gpslistener)
//            mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000, 5, gps_locationer)
//            mgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 15000, 5, gps_locationer)

            val criteria = Criteria()
            criteria.altitudeRequired = false
            criteria.bearingRequired = false
            criteria.isCostAllowed = false
            criteria.powerRequirement = Criteria.POWER_LOW
            criteria.accuracy = Criteria.ACCURACY_FINE
            val providerFine = mgr.getBestProvider(criteria, true)

            criteria.accuracy = Criteria.ACCURACY_COARSE
            val providerCoarse = mgr.getBestProvider(criteria, true)

            if (providerCoarse != null) {
                mgr.requestLocationUpdates(providerCoarse, 30000, 5f, network_locationer)
            }
            if (providerFine != null) {
                mgr.requestLocationUpdates(providerFine, 2000, 0f, gps_locationer)
            }
        }
    }

    override fun onCreate() {
        mNM = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        LocalBroadcastManager.getInstance(this).registerReceiver(
//                gpsStatusReceiver, IntentFilter("locationer"))

        showNotification()

        val thread = HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND)
        thread.start()

        mServiceLooper = thread.looper
        mServiceHandler = ServiceHandler(mServiceLooper)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(this, "local service is started ", Toast.LENGTH_SHORT).show()
        mu.appendLog(DEBUG_TAG, "=====tracking is started=====")

        val msg = mServiceHandler.obtainMessage()
        msg.arg1 = startId
        mServiceHandler.sendMessage(msg)

        return START_STICKY
    }

    override fun onDestroy() {
        mNM.cancel(NOTIFICATION)
        mgr.removeUpdates(gps_locationer)
        mgr.removeUpdates(network_locationer)
//        mgr.removeGpsStatusListener(gpslistener)
        Toast.makeText(this, "local service is stopped", Toast.LENGTH_SHORT).show()
        mu.appendLog(DEBUG_TAG, "=====tracking is stopped=====")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun showNotification() {
        val text = getText(R.string.local_service_started)
        val contentIntent = PendingIntent.getActivity(this, 0,
                Intent(this, TrackingActivity::class.java), 0)

        builder = NotificationCompat.Builder(baseContext)
                .setContentTitle("You are being tracked...")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(contentIntent)
                .setOngoing(true)

        val notification = builder.build()

        mNM.notify(NOTIFICATION, notification)
    }

//    private val gpsStatusReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            last_time = intent.getLongExtra("lasttime", 0)
//        }
//    }

//    private inner class gpsStatusListener : GpsStatus.Listener {
//
//        override fun onGpsStatusChanged(event: Int) {
//            var isGPSFix = false
//
//            when (event) {
//                GpsStatus.GPS_EVENT_SATELLITE_STATUS -> {
//                    if (last_time != 0L) {
//                        isGPSFix = (SystemClock.elapsedRealtime() - last_time) < 10000
//                    }
//                    if (isGPSFix) {
//                        mu.appendLog(DEBUG_TAG, "GPS has a fix")
//                        Log.d(DEBUG_TAG, "GPS has a fix")
//                    } else {
//                        mu.appendLog(DEBUG_TAG, "GPS does not has a fix")
//                        Log.d(DEBUG_TAG, "GPS does not have a fix")
//                    }
//                }
//                GpsStatus.GPS_EVENT_FIRST_FIX -> {
//                    isGPSFix = true
//                    mu.appendLog(DEBUG_TAG, "GPS first fix")
//                    Log.d(DEBUG_TAG, "GPS first fix")
//                }
//                GpsStatus.GPS_EVENT_STARTED -> {
//                    mu.appendLog(DEBUG_TAG, "GPS started")
//                    Log.i("GPS", "Started!")
//                }
//                GpsStatus.GPS_EVENT_STOPPED -> {
//                    mu.appendLog(DEBUG_TAG, "GPS stopped")
//                    Log.i("GPS", "Stopped")
//                }
//            }
//
//        }
//
//    }

    companion object {
        private const val DEBUG_TAG = "MyService"
    }
}
