package com.kodabots.sdk.core

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.webkit.WebView
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Locale


object KodaBotsSDK {

    private const val KODA_CLIENT_TOKEN_KEY = "com.kodabots.sdk.ClientToken"

    internal val globalExceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e("KodaBotsSDK", "Coroutine exception: ${exception.message}")
    }

    private val kodaClientScope = CoroutineScope(SupervisorJob() + globalExceptionHandler)

    var isInitialized = false
        private set

    /**
     * Change this field only for debug
     */
    var clientToken: String? = null
    private var restApi: KodaBotsRestApi? = null

    /**
     * Method used to initialize SDK.
     * Fetches ClientToken from Manifest file.
     *
     * @param context
     * @return Boolean value that indicates init state
     */
    fun init(context: Context): Boolean {
        clientToken = context.packageManager
            .getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            ).metaData?.getString(KODA_CLIENT_TOKEN_KEY)

        if (clientToken != null) {
            isInitialized = true
            restApi = KodaBotsRestApi()
            KodaBotsPreferences.initialize(context)
        } else {
            Log.d("KodaBotsSDK", "Failed to get ClientId, please check your AndroidManifest")
        }

        return isInitialized
    }


    /**
     * Method used to uninitialize SDK.
     */
    fun uninitialize() {
        kodaClientScope.cancel("Koda SDK uninitialized")
        isInitialized = false
    }

    internal fun gatherPhoneData(context: Context, userProfile: UserProfile? = null): UserProfile? {
        Log.d(
            "KodaBotsSDK",
            "Manufacturer: ${Build.MANUFACTURER} ; Model: ${Build.MODEL} ; SDK: ${Build.VERSION.SDK_INT} ; User Agent String: ${
                WebView(context).settings.userAgentString
            } ; Language: ${Locale.getDefault().displayCountry}"
        )
        userProfile?.apply {
            this.manufacturer = Build.MANUFACTURER
            this.model = Build.MODEL
            this.os = "Android"
            this.os_version = Build.VERSION.SDK_INT.toString()
            this.webview_user_agent = WebView(context).settings.userAgentString
            this.locale = Locale.getDefault().displayCountry
        }
        return userProfile
    }

    /**
     * Method used to get unread messages count
     *
     * @param callback Callback that returns sealed class with result
     */
    fun getUnreadCount(callback: (CallResponse<Int?>) -> Unit) {
        if (KodaBotsPreferences.userId != null && clientToken != null) {
            kodaClientScope.launch(globalExceptionHandler) {
                when (val response = restApi?.getUnreadCount()) {
                    is CallResponse.Success -> {
                        callback.invoke(response)
                    }

                    else -> {
                        callback.invoke(
                            response
                                ?: CallResponse.Error(Exception("Rest API not initialized properly"))
                        )
                    }
                }
            }
        } else {
            callback.invoke(CallResponse.Error(Exception("UserID or ClientID are null")))
        }
    }


    /**
     * Method used to get unread messages count
     *
     * @return Sealed class with result
     */
    suspend fun getUnreadCount(): CallResponse<Int?> {
        return if (KodaBotsPreferences.userId != null && clientToken != null)
            restApi?.getUnreadCount()
                ?: CallResponse.Error(Exception("Rest API not initialized properly")) else {
            CallResponse.Error(Exception("UserID or ClientID are null"))
        }
    }

    /**
     * If SDK is initialized, will return KodaBotsWebViewFragment that you can display and use.
     *
     * @param config Configuration file for SDK. You can set userProfile, conversation blockId and progress/timeout ui customisation
     * @param callbacks Callbacks from KodaBots chatbot
     * @return KodaBotsWebViewFragment
     */
    fun generateFragment(
        config: KodaBotsConfig? = null,
        callbacks: ((KodaBotsCallbacks) -> Unit)? = null
    ): KodaBotsWebViewFragment? {
        return if (isInitialized) KodaBotsWebViewFragment().apply {
            config?.let {
                customConfig = it
            }
            callbacks?.let {
                this.callbacks = it
            }
        } else null
    }
}


// TODO: Remove if no more needed
enum class PermissionRequestResult {
    GRANTED, DENIED, PERNAMENTLY_DENIED, RATIONAL
}