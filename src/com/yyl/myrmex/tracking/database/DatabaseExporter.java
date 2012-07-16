package com.yyl.myrmex.tracking.database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

public class DatabaseExporter {

   private static final String DATASUBDIRECTORY = "/dev_export";
   private static final String DEBUG_TAG = "Exporter";
   private SQLiteDatabase db;
   private TextBuilder textBuilder;

   public DatabaseExporter(SQLiteDatabase db) {
      this.db = db;
   }

   public void export(String dbName, String exportFileNamePrefix) throws IOException {
      Log.i(DEBUG_TAG, "exporting database - " + dbName + " exportFileNamePrefix=" + exportFileNamePrefix);

      this.textBuilder = new TextBuilder();
      this.textBuilder.start(dbName);

      // get the tables
      String sql = "select * from sqlite_master";
      Cursor c = this.db.rawQuery(sql, new String[0]);
      Log.d(DEBUG_TAG, "select * from sqlite_master, cur size " + c.getCount());
      if (c.moveToFirst()) {
         do {
            String tableName = c.getString(c.getColumnIndex("name"));
            Log.d(DEBUG_TAG, "table name " + tableName);

            // skip metadata, sequence, and uidx (unique indexes)
            if (!tableName.equals("android_metadata") && !tableName.equals("sqlite_sequence")
                     && !tableName.startsWith("uidx")) {
               this.exportTable(tableName);
            }
         } while (c.moveToNext());
      }
      String output = this.textBuilder.end();
      this.writeToFile(output, exportFileNamePrefix + ".txt");
      Log.i(DEBUG_TAG, "exporting database complete");
   }

   private void exportTable(final String tableName) throws IOException {
      Log.d(DEBUG_TAG, "exporting table - " + tableName);
      this.textBuilder.openTable(tableName);
      String sql = "select * from " + tableName;
      Cursor c = this.db.rawQuery(sql, new String[0]);
      if (c.moveToFirst()) {
         int cols = c.getColumnCount();
         do {
            this.textBuilder.openRow();
            for (int i = 0; i < cols; i++) {
               this.textBuilder.addColumn(c.getColumnName(i), c.getString(i));
            }
            this.textBuilder.closeRow();
         } while (c.moveToNext());
      }
      c.close();
      this.textBuilder.closeTable();
   }

   private void writeToFile(String xmlString, String exportFileName) throws IOException {
      File dir = new File(Environment.getExternalStorageDirectory(), DATASUBDIRECTORY);
      if (!dir.exists()) {
         dir.mkdirs();
      }
      File file = new File(dir, exportFileName);
      file.createNewFile();

      ByteBuffer buff = ByteBuffer.wrap(xmlString.getBytes());
      FileChannel channel = new FileOutputStream(file).getChannel();
      try {
         channel.write(buff);
      } finally {
         if (channel != null)
            channel.close();
      }
   }

   class TextBuilder {
      private static final String OPEN_XML_STANZA = "";
      private static final String CLOSE_WITH_TICK = "'>\n";
      private static final String DB_OPEN = "<database name='";
      private static final String DB_CLOSE = "";
      private static final String TABLE_OPEN = "<table name='";
      private static final String TABLE_CLOSE = "";
      private static final String ROW_OPEN = "\n";
      private static final String ROW_CLOSE = "";
      private static final String COL_OPEN = "<col name='";
      private static final String COL_CLOSE = "|";

      private final StringBuilder sb;

      public TextBuilder() throws IOException {
         this.sb = new StringBuilder();
      }

      void start(String dbName) {
         this.sb.append(OPEN_XML_STANZA);
         this.sb.append(DB_OPEN + dbName + CLOSE_WITH_TICK);
      }

      String end() throws IOException {
         this.sb.append(DB_CLOSE);
         return this.sb.toString();
      }

      void openTable(String tableName) {
         this.sb.append(TABLE_OPEN + tableName + CLOSE_WITH_TICK);
      }

      void closeTable() {
         this.sb.append(TABLE_CLOSE);
      }

      void openRow() {
         this.sb.append(ROW_OPEN);
      }

      void closeRow() {
         this.sb.append(ROW_CLOSE);
      }

      void addColumn(final String name, final String val) throws IOException {
//         this.sb.append(COL_OPEN + name + CLOSE_WITH_TICK + val + COL_CLOSE);
    	  this.sb.append(name + ": " + val + COL_CLOSE);
      }
   }
}
