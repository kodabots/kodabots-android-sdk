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
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import androidx.fragment.app.Fragment


class KodaBotsChromeClient(
    private val fragment: Fragment,
    private val fileChooserLauncher: FileChooserLauncher
) : WebChromeClient() {
    private var customView: View? = null
    private var originalOrientation: Int = 0
    private var originalSystemUiBehavior: Int = 0
    private var customViewCallback: CustomViewCallback? = null

    var filePathCallback: ValueCallback<Array<Uri?>?>? = null

    @SuppressLint("WrongConstant")
    override fun onHideCustomView() {
        val window = fragment.requireActivity().window
        (window.decorView as FrameLayout).removeView(this.customView)
        this.customView = null

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior = this.originalSystemUiBehavior

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
        this.originalOrientation = fragment.requireActivity().requestedOrientation
        this.customViewCallback = paramCustomViewCallback

        val window = fragment.requireActivity().window

        (window.decorView as FrameLayout).addView(
            this.customView,
            FrameLayout.LayoutParams(-1, -1)
        )
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        this.originalSystemUiBehavior = insetsController.systemBarsBehavior
        insetsController.systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
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
            fileChooserLauncher.launchFileChooser(
                fileChooserParams.createIntent()
            )
        } catch (e: ActivityNotFoundException) {
            return false
        }
        return true
    }
}