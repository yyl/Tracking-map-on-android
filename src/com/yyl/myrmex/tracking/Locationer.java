package com.yyl.myrmex.tracking;

import com.yyl.myrmex.tracking.database.LocContentProvider;
import com.yyl.myrmex.tracking.database.LocTable;
import com.yyl.myrmex.tracking.places.MyPlaces;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;
import android.location.GpsStatus.Listener;

class Locationer implements LocationListener {

	private Context ctx;
	private MyPlaces mplace;
	private long mLastLocationMillis;
	private MyUtility mu;
	
	private static final String DEBUG_TAG = "Locationer";
	private static final String[] Status = {"out of service", "temporarily unavailable", "available"};
	private static final double ACCU_THRESHOLD = 100.0;
	
	public Locationer(Context context) {
		ctx = context;
		mu = new MyUtility();
		mplace = new MyPlaces();
	}
	
	@Override
	public void onLocationChanged(Location location) {
//		Intent intent = new Intent("locationer");
        if ((location == null)||(location.getAccuracy() > ACCU_THRESHOLD)) {
        	mu.appendLog(DEBUG_TAG, "location unavailable.");
        	return;
        }
        mu.appendLog(DEBUG_TAG, "onLocationChanged method invoked by " + location.getProvider());
        mLastLocationMillis = SystemClock.elapsedRealtime();
        
//        intent.putExtra("lasttime", mLastLocationMillis);
//        intent.putExtra("lat", location.getLatitude());
//        intent.putExtra("lon", location.getLongitude());
//        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
        
        // Do something.
        insertLocation(location);
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.d(DEBUG_TAG, provider + " disabled.");
		mu.appendLog(DEBUG_TAG, "onProviderDisabled method invoked ->" + provider + "disabled.");
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.d(DEBUG_TAG, provider + " enabled.");
		mu.appendLog(DEBUG_TAG, "onProviderEnabled method invoked ->" + provider + "enabled.");
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d(DEBUG_TAG, provider + " statu changed" + status );
		mu.appendLog(DEBUG_TAG, "onStatusChanged method invoked ->" + provider + "changed into " + Status[status]);
	}
	
    /** Describe the given location, which might be null */
    private String dumpLocation(Location location) { 
    	String msg;
       	StringBuilder builder = new StringBuilder();
        builder
        	.append("P:")
    		.append(location.getProvider()) 
//    		.append("|V:" ) 
//    		.append(location.getSpeed()) 
    		.append("|A:" ) 
    		.append(location.getAccuracy());
//    		.append("|D:")
//    		.append(location.getBearing());
        	
        msg = builder.toString();
    	
    	return msg;
    }
    

	private void insertLocation(Location location) {
		double longitude;
		double latitude;
		String time;
		String extra;
		String places = "Place:N/A";
		String venues = "Venues:N/A";
		String result = "Location currently unavailable.";
		
		// get coordinates
		longitude = location.getLongitude();
		latitude = location.getLatitude();
		time = mu.parseTime(location.getTime());
		extra = dumpLocation(location);
		result = Double.toString(latitude)+", "+ Double.toString(longitude);
		// get places
		try {
			places = mplace.searchPlaces(longitude, latitude, 30);
		} catch (Exception e) {
			e.printStackTrace();
			mu.appendLog(DEBUG_TAG, e.getMessage());
		}
		// put them into db
		ContentValues values = new ContentValues(); 
		values.put(LocTable.COLUMN_TIME, time); 
		values.put(LocTable.COLUMN_LATITUDE, latitude);    		
		values.put(LocTable.COLUMN_LONGITUDE, longitude);
		values.put(LocTable.COLUMN_EXTRA, extra);
		values.put(LocTable.COLUMN_PLACES, places);
		values.put(LocTable.COLUMN_VENUES, venues);
		ctx.getContentResolver().insert(LocContentProvider.CONTENT_URI, values);
		mu.appendLog(DEBUG_TAG, "put in value " + result + " " + extra);
		Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
	}



}