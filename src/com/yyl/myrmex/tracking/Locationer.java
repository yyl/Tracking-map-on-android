package com.yyl.myrmex.tracking;


import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import com.yyl.myrmex.tracking.database.LocContentProvider;
import com.yyl.myrmex.tracking.database.LocTable;

import android.content.ContentValues;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;

class Locationer implements LocationListener {

	private Context ctx;
	
	public Locationer(Context context) {
		ctx = context;
	}
	
	@Override
	public void onLocationChanged(Location location) {
          insertLocation(location);
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
	}
	
    /** Describe the given location, which might be null */
    private String dumpLocation(Location location) { 
    	String msg;
    	if (location == null)
    		msg = "Location unavailable";
    	else
    	{
       		StringBuilder builder = new StringBuilder();
        	builder
        	.append("Prvdr:")
    		.append(location.getProvider()) 
//        	.append("|Time:")
//    		.append(parseTime(location.getTime())) 
    		.append("|Speed:" ) 
    		.append(location.getSpeed()) 
    		.append("|Accu:" ) 
    		.append(location.getAccuracy());
        	
        	msg = builder.toString();
    	}
    	
    	return msg;
    }
    

	private void insertLocation(Location location) {
		double longitude;
		double latitude;
		String time;
		String extra;
		String places = "Place:N/A";
		String venues = "Venue:N/A";
		String result = "Location currently unavailable.";
		
		// get coordinates
		longitude = location.getLongitude();
		latitude = location.getLatitude();
		time = parseTime(location.getTime());
		extra = dumpLocation(location);
		result = Double.toString(latitude)+", "+ Double.toString(longitude);
		// get places
//		try {
//			places = mplace.searchPlaces(longitude, latitude, 300);
//			venues = mplace.searchVenues(result);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		// connect with the web app
//		mplace.sendData(latitude, longitude);
		// put them into db
		ContentValues values = new ContentValues(); 
		values.put(LocTable.COLUMN_TIME, time); 
		values.put(LocTable.COLUMN_LATITUDE, latitude);    		
		values.put(LocTable.COLUMN_LONGITUDE, longitude);
		values.put(LocTable.COLUMN_EXTRA, extra);
		values.put(LocTable.COLUMN_PLACES, places);
		values.put(LocTable.COLUMN_VENUES, venues);
		ctx.getContentResolver().insert(LocContentProvider.CONTENT_URI, values);
		
		Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
	}
    
	private String parseTime(long t) {
		String format = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
//		DateFormat df = DateFormat.getTimeInstance(DateFormat.MEDIUM);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT-4"));
		String gmtTime = sdf.format(t);
		return gmtTime;
	}

}