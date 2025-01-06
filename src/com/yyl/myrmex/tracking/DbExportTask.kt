package com.yyl.myrmex.tracking

import android.content.Context
import android.os.AsyncTask
import android.os.Environment
import android.widget.Toast
import com.yyl.myrmex.tracking.database.DatabaseExporter
import com.yyl.myrmex.tracking.database.LocDatabaseHelper
import java.io.IOException
import java.util.*

class DbExportTask(private val ctx: Context) : AsyncTask<Void, Void, Boolean>() {

    private val calendar = Calendar.getInstance()
    private val mu = MyUtility()

    override fun doInBackground(vararg args: Void): Boolean? {
        val database = LocDatabaseHelper(ctx)
        val dbe = DatabaseExporter(database.readableDatabase)
        return try {
            val time = mu.parseTime(calendar.timeInMillis)
            dbe.export(Environment.getDataDirectory().toString() + LOCTABLE_PATH, EXPORT_FILENAME + time)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            mu.appendLog(DEBUG_TAG, e.message)
            false
        }
    }

    override fun onPostExecute(success: Boolean?) {
        if (success == true) {
            Toast.makeText(ctx, "Export successful!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(ctx, "Export failed", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val EXPORT_FILENAME = "trackingmap"
        private const val LOCTABLE_PATH = "/data/com.yyl.myrmex.trackingmap/databases/loctable.db"
        private const val DEBUG_TAG = "DbExpoertTask"
    }
}
