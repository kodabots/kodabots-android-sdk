package com.kodabots.sdk.core

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.net.Uri
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.fragment.app.Fragment


class KodaBotsChromeClient(private val fragment: Fragment) : WebChromeClient() {
    private var customView: View? = null
    private var originalOrientation: Int = 0
    private var originalSystemUiVisibility: Int = 0
    private var customViewCallback: CustomViewCallback? = null

    var filePathCallback: ValueCallback<Array<Uri?>?>? = null

    @SuppressLint("WrongConstant")
    override fun onHideCustomView() {
        (fragment.requireActivity().window.decorView as FrameLayout).removeView(this.customView)
        this.customView = null
        fragment.requireActivity().window.decorView.systemUiVisibility =
            this.originalSystemUiVisibility
        fragment.requireActivity().requestedOrientation = this.originalOrientation
        this.customViewCallback?.onCustomViewHidden()
        this.customViewCallback = null
    }

    override fun onShowCustomView(
        paramView: View,
        paramCustomViewCallback: CustomViewCallback
    ) {
        if (this.customView != null) {
            onHideCustomView()
            return
        }
        this.customView = paramView
        this.originalSystemUiVisibility =
            fragment.requireActivity().window.decorView.systemUiVisibility
        this.originalOrientation = fragment.requireActivity().requestedOrientation
        this.customViewCallback = paramCustomViewCallback
        (fragment.requireActivity().window.decorView as FrameLayout).addView(
            this.customView,
            FrameLayout.LayoutParams(-1, -1)
        )
        fragment.requireActivity().window.decorView.systemUiVisibility = 3846
    }

    override fun onPermissionRequest(request: PermissionRequest) {
        request.grant(request.resources)
    }

    override fun onShowFileChooser(
        mWebView: WebView?,
        filePathCallback: ValueCallback<Array<Uri?>?>,
        fileChooserParams: FileChooserParams
    ): Boolean {
        this.filePathCallback = filePathCallback
        try {
            fragment.startActivityForResult(
                fileChooserParams.createIntent(),
                REQUEST_SELECT_FILE,
                null
            )
        } catch (e: ActivityNotFoundException) {
            return false
        }
        return true
    }

    companion object {
        val REQUEST_SELECT_FILE = 882
    }
}