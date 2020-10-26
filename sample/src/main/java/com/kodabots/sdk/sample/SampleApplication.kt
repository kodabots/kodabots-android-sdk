package com.kodabots.sdk.sample

import android.app.Application
import android.util.Log
import com.kodabots.sdk.core.KodaBotsSDK

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        if (!KodaBotsSDK.init(this)) {
            Log.e("SampleApplication", "Initialization failed")
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        KodaBotsSDK.uninitialize()
    }

    companion object {
        lateinit var instance: SampleApplication
            private set
    }

}