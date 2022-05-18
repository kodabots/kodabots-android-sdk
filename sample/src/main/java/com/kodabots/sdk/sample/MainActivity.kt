package com.kodabots.sdk.sample

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.kodabots.sdk.core.*
import com.kodabots.sdk.sample.databinding.ActivityMainBinding
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.activityMainControlsExpander.setOnClickListener {
            binding.activityMainControlsWrapper.visibility =
                if (binding.activityMainControlsWrapper.visibility == View.GONE) View.VISIBLE else View.GONE
        }
        binding.activityMainControlsInitializeWebview.setOnClickListener {
            SingleEditTextDialog(this).apply {
                setText(
                    resources.getString(R.string.dialog_set_token),
                    null
                )
                setInitialValue(KodaBotsSDK.clientToken?:"")
            }.also {
                it.createDialog {
                    KodaBotsSDK.clientToken = it
                }
                it.mDialog?.setOnDismissListener {
                    if (kodaBotsFragment == null) {
                        kodaBotsFragment = KodaBotsSDK.generateFragment(
                            callbacks = callbacks,
                            config = KodaBotsConfig().apply {
                                progressConfig = KodaBotsProgressConfig()
                                progressConfig?.progressColor = Color.RED
                                progressConfig?.backgroundColor = Color.WHITE
                            }
                        )
                        supportFragmentManager.beginTransaction().apply {
                            replace(R.id.activity_main_content_root, kodaBotsFragment!!)
                            commit()
                        }
                    }
                }
            }
            binding.activityMainControlsExpander.callOnClick()
        }
        binding.activityMainControlsGetUnreadCount.setOnClickListener {
            scope.launch {
                KodaBotsSDK.getUnreadCount().let {
                    when (it) {
                        is CallResponse.Success -> {
                            Snackbar.make(
                                binding.activityMainRoot,
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
            binding.activityMainControlsExpander.callOnClick()
        }
        binding.activityMainControlsSyncProfile.setOnClickListener {
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
                            this.custom_parameters["custom_key"] = customKey
                        }) == false || kodaBotsFragment == null) {
                        Snackbar.make(
                            binding.activityMainRoot,
                            resources.getString(R.string.activity_main_controls_initialize_webview),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
            binding.activityMainControlsExpander.callOnClick()
        }
        binding.activityMainControlsSendBlock.setOnClickListener {
            SingleEditTextDialog(this).apply {
                setText(
                    resources.getString(R.string.activity_main_dialog_sync_profile),
                    null
                )
            }.also {
                it.createDialog {
                    if (kodaBotsFragment?.sendBlock(it) == false || kodaBotsFragment == null) {
                        Snackbar.make(
                            binding.activityMainRoot,
                            resources.getString(R.string.activity_main_controls_initialize_webview),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
            binding.activityMainControlsExpander.callOnClick()
        }
        binding.activityMainControlsSimulateError.setOnClickListener {
            if (kodaBotsFragment?.simulateError() == false || kodaBotsFragment == null) {
                Snackbar.make(
                    binding.activityMainRoot,
                    resources.getString(R.string.activity_main_controls_initialize_webview),
                    Snackbar.LENGTH_LONG
                ).show()
            }
            binding.activityMainControlsExpander.callOnClick()
        }
//        scope.async(Dispatchers.Main) {
//            KodaBotsSDK.requestPermissions(this@MainActivity, PERMISSIONS_REQUEST_CODE)
//        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}