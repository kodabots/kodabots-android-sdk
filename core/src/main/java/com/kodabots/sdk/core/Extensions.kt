package com.kodabots.sdk.core

import android.util.Log
import android.webkit.WebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception

fun WebView.callJavascript(function: String) {
    val webview = this
    try {
        Log.d("KodaBotsSDK", "Calling Javascript: ${function}")
    } catch (e:Exception){
        Log.e("KodaBotsSDK", e.message, e)
        // Do nothing, ignore
    }
    GlobalScope.launch(Dispatchers.Main) {
        webview.loadUrl("javascript:$function")
    }
}
