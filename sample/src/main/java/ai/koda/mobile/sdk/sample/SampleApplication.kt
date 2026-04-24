package ai.koda.mobile.sdk.sample

import ai.koda.mobile.core_shared.KodaBotsSDK
import android.app.Application

class SampleApplication : Application() {

    override fun onTerminate() {
        super.onTerminate()
        KodaBotsSDK.uninitialize()
    }
}