package com.yyl.myrmex.tracking

import android.content.Intent
import android.database.Cursor
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import com.google.android.maps.GeoPoint
import com.google.android.maps.MapActivity
import com.google.android.maps.MapController
import com.google.android.maps.MapView
import com.google.android.maps.Overlay
import com.google.android.maps.OverlayItem
import com.google.android.maps.Projection
import com.yyl.myrmex.tracking.database.LocContentProvider
import com.yyl.myrmex.tracking.database.LocTable

class TrackingActivity : MapActivity() {

    private lateinit var b1: Button
    private lateinit var b2: Button
    private lateinit var b3: Button
    private lateinit var b4: Button
    private lateinit var b5: Button
    private lateinit var mapView: MapView
    private lateinit var mapOverlays: List<Overlay>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        b1 = findViewById(R.id.start)
        b1.setOnClickListener(mStartListener)
        b2 = findViewById(R.id.stop)
        b2.setOnClickListener(mStopListener)
        b3 = findViewById(R.id.delete)
        b3.setOnClickListener(mDeleteListener)
        b4 = findViewById(R.id.export)
        b4.setOnClickListener(mExportListener)
        b5 = findViewById(R.id.draw)
        b5.setOnClickListener(mDrawListener)

        mapView = findViewById(R.id.mapview)
        mapView.isBuiltInZoomControls = true
        mapOverlays = mapView.overlays
        drawPath()
    }

    private val mStartListener = View.OnClickListener {
        val i = Intent(this@TrackingActivity, MyService::class.java)
        startService(i)
    }

    private val mStopListener = View.OnClickListener {
        stopService(Intent(this@TrackingActivity, MyService::class.java))
    }

    private val mDeleteListener = View.OnClickListener {
        val match = arrayOf("0")
        contentResolver.delete(LocContentProvider.CONTENT_URI, LocTable.COLUMN_ID + " > ?", match)
    }

    private val mExportListener = View.OnClickListener {
        val task = DbExportTask(applicationContext)
        task.execute()
    }

    private val mDrawListener = View.OnClickListener {
        drawPath()
    }

    override fun isRouteDisplayed(): Boolean {
        return false
    }

    private fun drawPath() {
        mapOverlays.clear()
        val drawable = this.resources.getDrawable(R.drawable.pin)
        val itemizedoverlay = MyMapOverlay(drawable, this)

        val points = getPoints()

        for (i in 1 until points.size) {
            val linoverlay = LineOverlay(points[i - 1], points[i])
            mapOverlays.add(linoverlay)
        }

        val start = OverlayItem(points[0], "The starting point", points[0].toString())
        itemizedoverlay.addOverlay(start)
        val stop = OverlayItem(points[points.size - 1], "The end point", points[points.size - 1].toString())
        itemizedoverlay.addOverlay(stop)
        mapOverlays.add(itemizedoverlay)

        val myMapController = mapView.controller
        myMapController.setCenter(points[points.size - 1])
        myMapController.setZoom(18)
    }

    private fun getPoints(): Array<GeoPoint> {
        var count = 0
        val mProjection = arrayOf(LocTable.COLUMN_ID, LocTable.COLUMN_LONGITUDE, LocTable.COLUMN_LATITUDE)
        val mSelectionClause = LocTable.COLUMN_ID + "> ?"
        val mSelectionArgs = arrayOf("0")
        val mSortOrder = LocTable.COLUMN_ID

        val mCursor: Cursor? = contentResolver.query(
                LocContentProvider.CONTENT_URI,
                mProjection,
                mSelectionClause,
                mSelectionArgs,
                mSortOrder
        )

        Log.i("cursor", "Count: " + mCursor?.count + "|Column count: " + mCursor?.columnCount)

        return if (mCursor != null && mCursor.count != 0) {
            val all = arrayOfNulls<GeoPoint>(mCursor.count)

            if (mCursor.moveToFirst()) {
                do {
                    val lon = mCursor.getFloat(1)
                    val lat = mCursor.getFloat(2)
                    val point = GeoPoint((lat * 1E6).toInt(), (lon * 1E6).toInt())
                    all[count] = point
                    count++
                } while (mCursor.moveToNext())
            }
            mCursor.close()
            all.filterNotNull().toTypedArray()
        } else {
            arrayOf(GeoPoint(19240000, -99120000))
        }
    }

    private inner class LineOverlay(private val gp1: GeoPoint, private val gp2: GeoPoint) : Overlay() {

        override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean, when: Long): Boolean {
            val projection = mapView.projection
            if (!shadow) {
                val paint = Paint()
                paint.isAntiAlias = true
                val point = Point()
                projection.toPixels(gp1, point)
                paint.color = Color.BLUE
                val point2 = Point()
                projection.toPixels(gp2, point2)
                paint.strokeWidth = 2f
                canvas.drawLine(point.x.toFloat(), point.y.toFloat(), point2.x.toFloat(), point2.y.toFloat(), paint)
            }
            return super.draw(canvas, mapView, shadow, when)
        }
    }
}
