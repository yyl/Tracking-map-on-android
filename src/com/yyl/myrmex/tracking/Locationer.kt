package com.yyl.myrmex.tracking

import android.content.ContentValues
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import com.yyl.myrmex.tracking.database.LocContentProvider
import com.yyl.myrmex.tracking.database.LocTable
import com.yyl.myrmex.tracking.places.MyPlaces

class Locationer(context: Context) : LocationListener {

    private val ctx: Context = context
    private val mplace: MyPlaces = MyPlaces()
    private var mLastLocationMillis: Long = 0
    private val mu: MyUtility = MyUtility()

    override fun onLocationChanged(location: Location) {
        if (location == null || location.accuracy > ACCU_THRESHOLD) {
            mu.appendLog(DEBUG_TAG, "location unavailable.")
            return
        }
        mu.appendLog(DEBUG_TAG, "onLocationChanged method invoked by " + location.provider)
        mLastLocationMillis = SystemClock.elapsedRealtime()

        // Do something.
        insertLocation(location)
    }

    override fun onProviderDisabled(provider: String) {
        Log.d(DEBUG_TAG, "$provider disabled.")
        mu.appendLog(DEBUG_TAG, "onProviderDisabled method invoked ->$provider disabled.")
    }

    override fun onProviderEnabled(provider: String) {
        Log.d(DEBUG_TAG, "$provider enabled.")
        mu.appendLog(DEBUG_TAG, "onProviderEnabled method invoked ->$provider enabled.")
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        Log.d(DEBUG_TAG, "$provider statu changed$status")
        mu.appendLog(DEBUG_TAG, "onStatusChanged method invoked ->$provider changed into " + Status[status])
    }

    private fun dumpLocation(location: Location): String {
        val builder = StringBuilder()
        builder.append("P:")
            .append(location.provider)
            .append("|A:")
            .append(location.accuracy)
        return builder.toString()
    }

    private fun insertLocation(location: Location) {
        val longitude = location.longitude
        val latitude = location.latitude
        val time = mu.parseTime(location.time)
        val extra = dumpLocation(location)
        val result = "$latitude, $longitude"
        val places = try {
            mplace.searchPlaces(longitude, latitude, 30)
        } catch (e: Exception) {
            e.printStackTrace()
            mu.appendLog(DEBUG_TAG, e.message)
            "Place:N/A"
        }
        val venues = "Venues:N/A"
        val values = ContentValues()
        values.put(LocTable.COLUMN_TIME, time)
        values.put(LocTable.COLUMN_LATITUDE, latitude)
        values.put(LocTable.COLUMN_LONGITUDE, longitude)
        values.put(LocTable.COLUMN_EXTRA, extra)
        values.put(LocTable.COLUMN_PLACES, places)
        values.put(LocTable.COLUMN_VENUES, venues)
        ctx.contentResolver.insert(LocContentProvider.CONTENT_URI, values)
        mu.appendLog(DEBUG_TAG, "put in value $result $extra")
        Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val DEBUG_TAG = "Locationer"
        private const val ACCU_THRESHOLD = 100.0
        private val Status = arrayOf("out of service", "temporarily unavailable", "available")
    }
}
