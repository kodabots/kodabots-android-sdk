package com.kodabots.sdk.core

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.webkit.WebView
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


object KodaBotsSDK {
    internal val globalExceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e("KodaBotsSDK", "Coroutine exception: ${exception.message}")
    }

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
            ).metaData?.getString("com.kodabots.sdk.ClientToken")
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

    /*
    fun requestPermissions(
        activity: AppCompatActivity,
        requestCode: Int,
        callback: (PermissionRequestResult) -> Unit
    ) {
        GlobalScope.async(globalExceptionHandler) {
            if (!hasPermissions(activity)) {
                val result = PermissionManager.requestPermissions(
                    activity, requestCode,
                    Manifest.permission.INTERNET,
                    Manifest.permission.READ_EXTERNAL_STORAGE
//                    Manifest.permission.RECORD_AUDIO,
//                    Manifest.permission.MODIFY_AUDIO_SETTINGS
                )

                when (result) {
                    is PermissionResult.PermissionGranted -> {
                        callback.invoke(PermissionRequestResult.GRANTED)
                    }
                    is PermissionResult.PermissionDenied -> {
                        callback.invoke(PermissionRequestResult.DENIED)
                    }
                    is PermissionResult.ShowRational -> {
                        requestPermissions(activity, requestCode, callback)

                    }
                    is PermissionResult.PermissionDeniedPermanently -> {
                        callback.invoke(PermissionRequestResult.PERNAMENTLY_DENIED)
                    }
                }
            } else {
                callback.invoke(PermissionRequestResult.GRANTED)
            }
        }
    }*/

    /*
    suspend fun requestPermissions(
        activity: AppCompatActivity,
        requestCode: Int
    ): PermissionRequestResult {
        if (!hasPermissions(activity)) {
            val result = PermissionManager.requestPermissions(
                activity, requestCode,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_EXTERNAL_STORAGE,
//                Manifest.permission.RECORD_AUDIO,
//                Manifest.permission.MODIFY_AUDIO_SETTINGS
            )
            return when (result) {
                is PermissionResult.PermissionGranted -> {
                    PermissionRequestResult.GRANTED
                }
                is PermissionResult.PermissionDenied -> {
                    PermissionRequestResult.DENIED
                }
                is PermissionResult.ShowRational -> {
                    requestPermissions(activity, requestCode)
                }
                is PermissionResult.PermissionDeniedPermanently -> {
                    PermissionRequestResult.DENIED
                }
            }
        } else {
            return PermissionRequestResult.GRANTED
        }
    }
*/
    /*
    fun hasPermissions(context: Context): Boolean = EasyPermissions.hasPermissions(
        context,
        Manifest.permission.INTERNET,
        Manifest.permission.READ_EXTERNAL_STORAGE,
//        Manifest.permission.RECORD_AUDIO,
//        Manifest.permission.MODIFY_AUDIO_SETTINGS
    )

     */

    /**
     * Method used to get unread messages count
     *
     * @param callback Callback that returns sealed class with result
     */
    fun getUnreadCount(callback: (CallResponse<Int?>) -> Unit) {
        if (KodaBotsPreferences.userId != null && clientToken != null) {
            GlobalScope.launch(globalExceptionHandler) {
                when (val response = restApi?.getUnreadCount()?.await()) {
                    is CallResponse.Success -> {
                        callback.invoke(response)
                    }
                    else -> {
                        callback.invoke(
                            response
                                ?: CallResponse.Error<Int?>(Exception("Rest API not initialized properly"))
                        )
                    }
                }
            }
        } else {
            callback.invoke(CallResponse.Error<Int?>(Exception("UserID or ClientID are null")))
        }
    }


    /**
     * Method used to get unread messages count
     *
     * @return Sealed class with result
     */
    suspend fun getUnreadCount(): CallResponse<Int?> {
        return if (KodaBotsPreferences.userId != null && clientToken != null) restApi?.getUnreadCount()
            ?.await()
            ?: CallResponse.Error<Int?>(Exception("Rest API not initialized properly")) else {
            CallResponse.Error<Int?>(Exception("UserID or ClientID are null"))
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
        config: KodaBotsConfig?=null,
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

sealed class CallResponse<T> {
    class Success<T>(val value: T) : CallResponse<T>()
    class Error<T>(val exception: Exception) : CallResponse<T>()
    class Timeout<T> : CallResponse<T>()
}

enum class PermissionRequestResult {
    GRANTED, DENIED, PERNAMENTLY_DENIED, RATIONAL
}