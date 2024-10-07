package ai.koda.mobile.sdk.core

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class KodaBotsWebViewClient(
    private val callback: WebviewCallbacks
): WebViewClient(){
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        callback.onLoadingFinished()
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return callback.shouldOverrideUrlLoading(view, request)
    }
}

interface WebviewCallbacks {
    fun onLoadingFinished()
    fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean
}