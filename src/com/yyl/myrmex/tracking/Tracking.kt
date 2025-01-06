package com.yyl.myrmex.tracking

import android.app.Application
import android.content.Context

class Tracking : Application() {

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    companion object {
        lateinit var context: Context
    }
}
