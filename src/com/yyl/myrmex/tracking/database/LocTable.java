package com.yyl.myrmex.tracking.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class LocTable {

	// Database table
	public static final String TABLE_TODO = "loc";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TIME = "time";
	public static final String COLUMN_LONGITUDE = "longitude";
	public static final String COLUMN_LATITUDE = "latitude";
	public static final String COLUMN_EXTRA = "extra";
	public static final String COLUMN_PLACES = "places";
	public static final String COLUMN_VENUES = "venues";

	// Database creation SQL statement
	private static final String DATABASE_CREATE = "create table " 
			+ TABLE_TODO
			+ " (" 
			+ COLUMN_ID + " integer primary key autoincrement, " 
			+ COLUMN_TIME + " text not null, "
			+ COLUMN_LONGITUDE + " text not null, " 
			+ COLUMN_LATITUDE + " text not null, "
			+ COLUMN_EXTRA + " text not null, "
			+ COLUMN_PLACES + " text not null, "
			+ COLUMN_VENUES + " text not null"
			+ ");";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		Log.w(LocTable.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_TODO);
		onCreate(database);
	}
}