package ai.koda.mobile.sdk.sample

import ai.koda.mobile.core_shared.AndroidKodaBotsSDKDriver
import ai.koda.mobile.core_shared.KodaBotsSDK
import ai.koda.mobile.core_shared.config.KodaBotsConfig
import ai.koda.mobile.core_shared.config.KodaBotsProgressConfig
import ai.koda.mobile.core_shared.presentation.KodaBotsCallbacks
import android.app.Application
import android.graphics.Color
import android.util.Log

class SampleApplication : Application() {

    private val callbacks: (KodaBotsCallbacks) -> Unit = {
        when (it) {
            is KodaBotsCallbacks.Event -> {
                Log.d("KodaBotsSample", "CallbackEvent ${it.type} - ${it.params}")
            }

            is KodaBotsCallbacks.Error -> {
                Log.d("KodaBotsSample", "CallbackError ${it.error}")
            }
        }
    }


    override fun onCreate() {
        super.onCreate()
        instance = this
        if (!KodaBotsSDK.init(
                AndroidKodaBotsSDKDriver(
                    this,
                    callbacks = callbacks,
                    config = KodaBotsConfig().apply {
                        progressConfig = KodaBotsProgressConfig().apply {
                            progressColor = Color.RED
                            backgroundColor = Color.WHITE
                        }
                        noCameraPermissionInfo =
                            "No camera permission, you can only choose from your files."
                    }
                )
            )
        ) {
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