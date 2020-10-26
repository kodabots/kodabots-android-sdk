package com.kodabots.sdk.core

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ActivityNotFoundException
import android.net.Uri
import android.os.Build
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.fragment.app.Fragment


class KodaBotsChromeClient(val fragment: Fragment) : WebChromeClient() {
    private var mCustomView: View? = null
    private var mOriginalOrientation: Int = 0
    private var mOriginalSystemUiVisibility: Int = 0
    private var mCustomViewCallback: WebChromeClient.CustomViewCallback? = null
    var filePathCallback:ValueCallback<Array<Uri?>?>?=null

    @SuppressLint("WrongConstant")
    override fun onHideCustomView() {
        (fragment.requireActivity().window.decorView as FrameLayout).removeView(this.mCustomView)
        this.mCustomView = null
        fragment.requireActivity().window.decorView.systemUiVisibility =
            this.mOriginalSystemUiVisibility
        fragment.requireActivity().requestedOrientation = this.mOriginalOrientation
        this.mCustomViewCallback?.onCustomViewHidden()
        this.mCustomViewCallback = null
    }

    override fun onShowCustomView(
        paramView: View,
        paramCustomViewCallback: WebChromeClient.CustomViewCallback
    ) {
        if (this.mCustomView != null) {
            onHideCustomView()
            return
        }
        this.mCustomView = paramView
        this.mOriginalSystemUiVisibility =
            fragment.requireActivity().window.decorView.systemUiVisibility
        this.mOriginalOrientation = fragment.requireActivity().requestedOrientation
        this.mCustomViewCallback = paramCustomViewCallback
        (fragment.requireActivity().window.decorView as FrameLayout).addView(
            this.mCustomView,
            FrameLayout.LayoutParams(-1, -1)
        )
        fragment.requireActivity().window.decorView.systemUiVisibility = 3846
    }

    override fun onPermissionRequest(request: PermissionRequest) {
        if (KodaBotsSDK.hasPermissions(fragment.requireContext())) {
            request.grant(request.resources)
        }
    }

    override fun onShowFileChooser(
        mWebView: WebView?,
        filePathCallback: ValueCallback<Array<Uri?>?>,
        fileChooserParams: FileChooserParams
    ): Boolean {
        this.filePathCallback = filePathCallback
        try {
            fragment.startActivityForResult(fileChooserParams.createIntent(), REQUEST_SELECT_FILE, null)
        } catch (e: ActivityNotFoundException) {
            return false
        }
        return true
    }

    companion object {
        val REQUEST_SELECT_FILE = 882
    }
}