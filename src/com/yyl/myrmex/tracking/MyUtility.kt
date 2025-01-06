package com.yyl.myrmex.tracking

import android.os.Environment
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MyUtility {

    private val logFile: File
    private val calendar = Calendar.getInstance()

    init {
        val dir = File(Environment.getExternalStorageDirectory(), LOG_PATH)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        logFile = File(dir, "log.txt")
        if (!logFile.exists()) {
            try {
                logFile.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun appendLog(tag: String?, text: String?) {
        val time = parseTime(calendar.timeInMillis)
        try {
            val buf = BufferedWriter(FileWriter(logFile, true))
            buf.append("$time@ $tag >>> $text")
            buf.newLine()
            buf.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun parseTime(t: Long): String {
        val format = "yyyy-MM-dd-HH-mm-ss"
        val sdf = SimpleDateFormat(format, Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("GMT-4")
        return sdf.format(t)
    }

    companion object {
        private const val LOG_PATH = "/tracking"
    }
}
