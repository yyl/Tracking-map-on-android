package com.yyl.myrmex.tracking.database

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Environment
import android.util.Log
import com.yyl.myrmex.tracking.MyUtility
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

class DatabaseExporter(private val db: SQLiteDatabase) {

    private lateinit var textBuilder: TextBuilder
    private val mu = MyUtility()

    @Throws(IOException::class)
    fun export(dbName: String, exportFileNamePrefix: String) {
        Log.i(DEBUG_TAG, "exporting database - $dbName exportFileNamePrefix=$exportFileNamePrefix")

        this.textBuilder = TextBuilder()
        this.textBuilder.start(dbName)

        val sql = "select * from sqlite_master"
        val c = this.db.rawQuery(sql, null)
        Log.d(DEBUG_TAG, "select * from sqlite_master, cur size " + c.count)
        if (c.moveToFirst()) {
            do {
                val tableName = c.getString(c.getColumnIndex("name"))
                Log.d(DEBUG_TAG, "table name $tableName")

                if (tableName != "android_metadata" && tableName != "sqlite_sequence" && !tableName.startsWith("uidx")) {
                    this.exportTable(tableName)
                }
            } while (c.moveToNext())
        }
        val output = this.textBuilder.end()
        this.writeToFile(output, "$exportFileNamePrefix.txt")
        Log.i(DEBUG_TAG, "exporting database complete")
    }

    @Throws(IOException::class)
    private fun exportTable(tableName: String) {
        Log.d(DEBUG_TAG, "exporting table - $tableName")
        this.textBuilder.openTable(tableName)
        val sql = "select * from $tableName"
        val c = this.db.rawQuery(sql, null)
        if (c.moveToFirst()) {
            val cols = c.columnCount
            do {
                this.textBuilder.openRow()
                for (i in 0 until cols) {
                    this.textBuilder.addColumn(c.getColumnName(i), c.getString(i))
                }
                this.textBuilder.closeRow()
            } while (c.moveToNext())
        }
        c.close()
        this.textBuilder.closeTable()
    }

    @Throws(IOException::class)
    private fun writeToFile(xmlString: String, exportFileName: String) {
        val dir = File(Environment.getExternalStorageDirectory(), DATASUBDIRECTORY)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, exportFileName)
        file.createNewFile()

        val buff = ByteBuffer.wrap(xmlString.toByteArray())
        val channel: FileChannel = FileOutputStream(file).channel
        try {
            channel.write(buff)
        } finally {
            if (channel != null) channel.close()
        }
    }

    inner class TextBuilder {

        private val sb = StringBuilder()

        fun start(dbName: String) {
            sb.append(DB_OPEN + dbName + CLOSE_WITH_TICK)
        }

        fun end(): String {
            return sb.toString()
        }

        fun openTable(tableName: String) {
            sb.append(TABLE_OPEN + tableName + CLOSE_WITH_TICK)
        }

        fun closeTable() {
            sb.append(TABLE_CLOSE)
        }

        fun openRow() {
            sb.append(ROW_OPEN)
        }

        fun closeRow() {
            sb.append(ROW_CLOSE)
        }

        @Throws(IOException::class)
        fun addColumn(name: String, val: String?) {
            sb.append(name + ": " + val + COL_CLOSE)
        }

        companion object {
            private const val CLOSE_WITH_TICK = "'>\n"
            private const val DB_OPEN = "<database name='"
            private const val DB_CLOSE = ""
            private const val TABLE_OPEN = "<table name='"
            private const val TABLE_CLOSE = ""
            private const val ROW_OPEN = "\n"
            private const val ROW_CLOSE = ""
            private const val COL_OPEN = "<col name='"
            private const val COL_CLOSE = "|"
        }
    }

    companion object {
        private const val DATASUBDIRECTORY = "/tracking"
        private const val DEBUG_TAG = "Exporter"
    }
}
