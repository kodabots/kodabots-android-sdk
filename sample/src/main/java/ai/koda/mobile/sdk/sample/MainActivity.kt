package ai.koda.mobile.sdk.sample

import ai.koda.mobile.core_shared.AndroidKodaBotsSDKDriver
import ai.koda.mobile.core_shared.KodaBotsSDK
import ai.koda.mobile.core_shared.config.KodaBotsConfig
import ai.koda.mobile.core_shared.config.KodaBotsProgressConfig
import ai.koda.mobile.core_shared.model.UserProfile
import ai.koda.mobile.core_shared.model.api.CallResponse
import ai.koda.mobile.core_shared.presentation.KodaBotsCallbacks
import ai.koda.mobile.core_shared.presentation.KodaBotsWebViewFragment
import ai.koda.mobile.sdk.sample.databinding.ActivityMainBinding
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var kodaBotsFragment: KodaBotsWebViewFragment? = null
    private val callbacks: (KodaBotsCallbacks) -> Unit = {
        when (it) {
            is KodaBotsCallbacks.Event -> Log.d("KodaBotsSample", "CallbackEvent ${it.type} - ${it.params}")
            is KodaBotsCallbacks.Error -> Log.d("KodaBotsSample", "CallbackError ${it.error}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        consumePadding()
        binding.activityMainControlsExpander.setOnClickListener {
            binding.activityMainControlsWrapper.visibility =
                if (binding.activityMainControlsWrapper.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        binding.activityMainControlsInitializeWebview.setOnClickListener {
            ThreeEditTextDialog(this).apply {
                setText(
                    resources.getString(R.string.activity_main_controls_initialize_webview),
                    null,
                    resources.getString(R.string.dialog_set_token),
                    resources.getString(R.string.dialog_custom_base_url),
                    resources.getString(R.string.dialog_custom_rest_url),
                )
                setInitialValues(BuildConfig.KODA_AI_CLIENT_TOKEN)
            }.also {
                it.createDialog { token, baseUrl, restUrl ->
                    val config = KodaBotsConfig().apply {
                        customClientToken = token.ifEmpty { null }
                        customBaseUrl = baseUrl.ifEmpty { null }
                        customBaseRestUrl = restUrl.ifEmpty { null }
                        progressConfig = KodaBotsProgressConfig().apply {
                            progressColor = Color.RED
                            backgroundColor = Color.WHITE
                        }
                        noCameraPermissionInfo = "No camera permission, you can only choose from your files."
                    }
                    if (KodaBotsSDK.init(AndroidKodaBotsSDKDriver(this, config = config, callbacks = callbacks))) {
                        kodaBotsFragment = KodaBotsSDK.driver?.generateScreen() as? KodaBotsWebViewFragment
                        kodaBotsFragment?.let { fragment ->
                            supportFragmentManager.beginTransaction().apply {
                                replace(R.id.activity_main_content_root, fragment)
                                commit()
                            }
                        }
                    } else {
                        Log.e("KodaBotsSample", "SDK initialization failed")
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
            SendBlockWithParamsDialog(this).also {
                it.createDialog { blockId, paramKey, paramValue ->
                    val param = mapOf(paramKey to paramValue)
                    if (kodaBotsFragment?.sendBlock(
                            blockId,
                            param
                        ) == false || kodaBotsFragment == null
                    ) {
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
    }

    private fun consumePadding() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(
                left = bars.left,
                top = bars.top,
                right = bars.right,
                bottom = bars.bottom,
            )
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}