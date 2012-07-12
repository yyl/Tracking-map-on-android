package com.yyl.myrmex.tracking;

import com.yyl.myrmex.tracking.database.LocTable;
import com.yyl.myrmex.tracking.database.LocContentProvider;
import com.yyl.myrmex.tracking.DbExportTask;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;


public class TrackingActivity extends FragmentActivity
implements LoaderManager.LoaderCallbacks<Cursor>{
    /** Called when the activity is first created. */
	
	private Button b1, b2, b3, b4;
	SimpleCursorAdapter adapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        drawTable();
        // Watch for button clicks.
        b1 = (Button)findViewById(R.id.start);
        b1.setOnClickListener(mStartListener);
        b2 = (Button)findViewById(R.id.stop);
        b2.setOnClickListener(mStopListener);
        b3 = (Button)findViewById(R.id.delete);
        b3.setOnClickListener(mDeleteListener);
        b4 = (Button)findViewById(R.id.export);
        b4.setOnClickListener(mExportListener);

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
    
	private void drawTable() {
		// Fields from the database (projection)
		// Must include the _id column for the adapter to work
		String[] from = new String[] { LocTable.COLUMN_TIME, 
				LocTable.COLUMN_LONGITUDE, LocTable.COLUMN_LATITUDE};
		// Fields on the UI to which we map
		int[] to = new int[] { R.id.time, R.id.longitude, R.id.latitude};
		getSupportLoaderManager().initLoader(0, null, this);
		adapter = new SimpleCursorAdapter(this, R.layout.row, null, from,
 			to, 0);
		ListView listview = (ListView) findViewById(R.id.list);
		listview.setAdapter(adapter);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String[] projection = { LocTable.COLUMN_ID, LocTable.COLUMN_TIME, 
				LocTable.COLUMN_LONGITUDE, LocTable.COLUMN_LATITUDE };
		CursorLoader cursorLoader = new CursorLoader(this,
				LocContentProvider.CONTENT_URI, projection, null, null, null);
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor data) {
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		adapter.swapCursor(null);
	}     

}