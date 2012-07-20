package com.yyl.myrmex.tracking;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import android.os.Environment;

public class MyUtility {
	private static final String LOG_PATH = "/tracking";
	private File logFile, dir;
	private Calendar calendar;
	
	public MyUtility() {
	      dir = new File(Environment.getExternalStorageDirectory(), LOG_PATH);
	      if (!dir.exists()) {
	         dir.mkdirs();
	      }
		   logFile = new File(dir, "log.txt");
		   if (!logFile.exists())
		   {
		      try
		      {
		         logFile.createNewFile();
		      } 
		      catch (IOException e)
		      {
		         // TODO Auto-generated catch block
		         e.printStackTrace();
		      }
		   }		
	}
	
	public void appendLog(String tag, String text)
	{       
		calendar = Calendar.getInstance();
		String time = parseTime(calendar.getTimeInMillis());
	   try
	   {
	      //BufferedWriter for performance, true to set append to file flag
	      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
	      buf.append(time + "@ " + tag + " >>> " + text);
	      buf.newLine();
	      buf.close();
	   }
	   catch (IOException e)
	   {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	   }
	}
	
	public String parseTime(long t) {
		String format = "yyyy-MM-dd-HH-mm-ss";
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT-4"));
		String gmtTime = sdf.format(t);
		return gmtTime;
	}
}