package com.kodabots.sdk.sample

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.kodabots.sdk.core.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    val PERMISSIONS_REQUEST_CODE = 123
    var kodaBotsFragment: KodaBotsWebViewFragment? = null
    val callbacks: (KodaBotsCallbacks) -> Unit = {
        when (it) {
            is KodaBotsCallbacks.Event -> {
                Log.d("KodaBotsSample", "CallbackEvent ${it.type} - ${it.params}")
            }
            is KodaBotsCallbacks.Error -> {
                Log.d("KodaBotsSample", "CallbackError ${it.error}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activity_main_controls_expander.setOnClickListener {
            activity_main_controls_wrapper.visibility =
                if (activity_main_controls_wrapper.visibility == View.GONE) View.VISIBLE else View.GONE
        }
        activity_main_controls_initialize_webview.setOnClickListener {
            if (kodaBotsFragment == null) {
                kodaBotsFragment = KodaBotsSDK.generateFragment(
                    callbacks = callbacks,
                    progressColor = Color.RED,
                    backgroundColor = Color.BLACK
                )
                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.activity_main_content_root, kodaBotsFragment!!)
                    commit()
                }
            }
            activity_main_controls_expander.callOnClick()
        }
        activity_main_controls_get_unread_count.setOnClickListener {
            GlobalScope.launch {
                KodaBotsSDK.getUnreadCount()?.let {
                    when (it) {
                        is CallResponse.Success -> {
                            Snackbar.make(
                                activity_main_root,
                                String.format(
                                    resources.getString(R.string.activity_main_unread_count),
                                    it.value
                                ),
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                        is CallResponse.Error -> {
                            Log.d("KodaBotsSample", "Error: ${it.exception.message}")
                        }
                        is CallResponse.Timeout -> {
                            Log.d("KodaBotsSample", "Timeout")
                        }
                    }

                }
            }
            activity_main_controls_expander.callOnClick()
        }
        activity_main_controls_sync_profile.setOnClickListener {
            ThreeEditTextDialog(this).apply {
                setText(
                    resources.getString(R.string.activity_main_dialog_sync_profile),
                    null,
                    resources.getString(R.string.activity_main_dialog_sync_profile_first_name),
                    resources.getString(R.string.activity_main_dialog_sync_profile_last_name),
                    resources.getString(R.string.activity_main_dialog_sync_profile_custom_key),
                )
            }.also {
                it.createDialog { firstName, lastName, customKey ->
                    if (kodaBotsFragment?.syncProfile(UserProfile().apply {
                            this.first_name = firstName
                            this.last_name = lastName
                            this.custom_key = customKey
                        }) == false || kodaBotsFragment == null) {
                        Snackbar.make(
                            activity_main_root,
                            resources.getString(R.string.activity_main_controls_initialize_webview),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
            activity_main_controls_expander.callOnClick()
        }
        activity_main_controls_send_block.setOnClickListener {
            SingleEditTextDialog(this).apply {
                setText(
                    resources.getString(R.string.activity_main_dialog_sync_profile),
                    null
                )
            }.also {
                it.createDialog {
                    if (kodaBotsFragment?.sendBlock(it) == false || kodaBotsFragment == null) {
                        Snackbar.make(
                            activity_main_root,
                            resources.getString(R.string.activity_main_controls_initialize_webview),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
            activity_main_controls_expander.callOnClick()
        }
        activity_main_controls_simulate_error.setOnClickListener {
            if (kodaBotsFragment?.simulateError() == false || kodaBotsFragment == null) {
                Snackbar.make(
                    activity_main_root,
                    resources.getString(R.string.activity_main_controls_initialize_webview),
                    Snackbar.LENGTH_LONG
                ).show()
            }
            activity_main_controls_expander.callOnClick()
        }
        GlobalScope.async(Dispatchers.Main) {
            KodaBotsSDK.requestPermissions(this@MainActivity, PERMISSIONS_REQUEST_CODE)
        }
    }
}