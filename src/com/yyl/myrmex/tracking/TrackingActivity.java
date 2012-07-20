package com.yyl.myrmex.tracking;

import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;
import com.yyl.myrmex.tracking.database.LocTable;
import com.yyl.myrmex.tracking.database.LocContentProvider;
import com.yyl.myrmex.tracking.DbExportTask;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;


public class TrackingActivity extends MapActivity{
    /** Called when the activity is first created. */
	
	private Button b1, b2, b3, b4, b5;
	SimpleCursorAdapter adapter;
	MapView mapView;
	List<Overlay> mapOverlays;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        // Watch for button clicks.
        b1 = (Button)findViewById(R.id.start);
        b1.setOnClickListener(mStartListener);
        b2 = (Button)findViewById(R.id.stop);
        b2.setOnClickListener(mStopListener);
        b3 = (Button)findViewById(R.id.delete);
        b3.setOnClickListener(mDeleteListener);
        b4 = (Button)findViewById(R.id.export);
        b4.setOnClickListener(mExportListener);
        b5 = (Button)findViewById(R.id.draw);
        b5.setOnClickListener(mDrawListener);
        
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapOverlays = mapView.getOverlays();
        drawPath();
    }
    
    private OnClickListener mStartListener = new OnClickListener() {
        public void onClick(View v) {
        	Intent i = new Intent(TrackingActivity.this, MyService.class);
            startService(i);
        }
    };

    private OnClickListener mStopListener = new OnClickListener() {
        public void onClick(View v) {
            // Cancel a previous call to startService().  Note that the
            // service will not actually stop at this point if there are
            // still bound clients.
            stopService(new Intent(TrackingActivity.this,
                    MyService.class));
        }
    };
    
    private OnClickListener mDeleteListener = new OnClickListener() {
        public void onClick(View v) {
        	// delete all current rows
    		String[] match = {"0"};
        	getContentResolver().delete(LocContentProvider.CONTENT_URI, LocTable.COLUMN_ID + " > ?", match);
        }
    };
    
    private OnClickListener mExportListener = new OnClickListener() {
        public void onClick(View v) {
        	// export all current rows
        	DbExportTask task = new DbExportTask(getApplicationContext());
    		task.execute();
        }
    };
    
    private OnClickListener mDrawListener = new OnClickListener() {
        public void onClick(View v) {
        	// export all current rows
        	drawPath();
        }
    };
    
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}   
	
    
    private void drawPath() {
    	mapOverlays.clear();
        Drawable drawable = this.getResources().getDrawable(R.drawable.pin);
//        Drawable begin = this.getResources().getDrawable(R.drawable.start);
//        Drawable end = this.getResources().getDrawable(R.drawable.end);
        MyMapOverlay itemizedoverlay = new MyMapOverlay(drawable, this);
        
        GeoPoint[] points = getPoints();
        
        for(int i = 1; i < points.length; i++) {
        	LineOverlay linoverlay = new LineOverlay(points[i - 1], points[i]);
            mapOverlays.add(linoverlay);
        }
        
        OverlayItem start = new OverlayItem(points[0], "The starting point", points[0].toString());
        itemizedoverlay.addOverlay(start);
        OverlayItem stop = new OverlayItem(points[points.length-1], "The end point", points[points.length-1].toString());
        itemizedoverlay.addOverlay(stop);
        mapOverlays.add(itemizedoverlay);
        
        MapController myMapController = mapView.getController();
        myMapController.setCenter(points[0]);
        myMapController.setZoom(18);
    }
	
	private GeoPoint[] getPoints() {
		GeoPoint[] all;
		int count = 0;
        String[] mProjection = {LocTable.COLUMN_ID, LocTable.COLUMN_LONGITUDE, LocTable.COLUMN_LATITUDE};
        String mSelectionClause = LocTable.COLUMN_ID + "> ?";
        String[] mSelectionArgs = {"0"};
		String mSortOrder = LocTable.COLUMN_ID;
        
        Cursor mCursor = getContentResolver().query(
        		LocContentProvider.CONTENT_URI,   // The content URI of the words table
        	    mProjection,                        // The columns to return for each row
        	    mSelectionClause,                    // Selection criteria
        	    mSelectionArgs,                     // Selection criteria
        	    mSortOrder); 
		
        Log.i("cursor", "Count: " + mCursor.getCount() + "|Column count: " + mCursor.getColumnCount());
        
        if(mCursor.getCount()!=0) {
            all = new GeoPoint[mCursor.getCount()];
            
            if (mCursor.moveToFirst()) {
                do {
                   float lon = mCursor.getFloat(1);
                   float lat = mCursor.getFloat(2);
                   GeoPoint point = new GeoPoint((int)(lat*1E6), (int)(lon*1E6));
                   all[count] = point;
                   count++;
                } while (mCursor.moveToNext());
            }
            mCursor.close();
        } else {
        	all = new GeoPoint[1];
        	all[0] = new GeoPoint(19240000,-99120000);
        }

		return all;
	}

    private class LineOverlay extends Overlay {

       private GeoPoint gp1;
       private GeoPoint gp2;

       public LineOverlay(GeoPoint gp1, GeoPoint gp2) {
           this.gp1 = gp1;
           this.gp2 = gp2;
       }

       @Override
       public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
               long when) {
           // TODO Auto-generated method stub
           Projection projection = mapView.getProjection();
           if (shadow == false) {

               Paint paint = new Paint();
               paint.setAntiAlias(true);
               Point point = new Point();
               projection.toPixels(gp1, point);
               paint.setColor(Color.BLUE);
               Point point2 = new Point();
               projection.toPixels(gp2, point2);
               paint.setStrokeWidth(2);
               canvas.drawLine((float) point.x, (float) point.y, (float) point2.x,(float) point2.y, paint);
           }
           return super.draw(canvas, mapView, shadow, when);
       }

       @Override
       public void draw(Canvas canvas, MapView mapView, boolean shadow) {
           // TODO Auto-generated method stub

           super.draw(canvas, mapView, shadow);
       }

   }

}