package ai.koda.mobile.core_shared.presentation

import ai.koda.mobile.core_shared.model.UserProfile
import platform.UIKit.UIViewController
import platform.darwin.NSObject

/**
 * Handle class to interact with KodaBotsWebViewScreen from iOS platform.
 * Provides methods to send blocks, sync user profile, and simulate errors.
 * This is walkaround for the lack of direct access to Kotlin subclasses from Swift/Objective-C
 * Caused error: "Kotlin subclass of Objective-C class can't be imported"
 */

class KodaBotsWebViewScreenHandle(
    webViewScreen: UIViewController
) {

    private val webViewScreenIos: IosKodaBotsWebViewScreen? =
        webViewScreen as? IosKodaBotsWebViewScreen

    fun sendBlock(blockId: String, params: Map<String, String>? = null): Boolean =
        webViewScreenIos?.sendBlock(blockId, params) ?: false

    fun syncUserProfile(profile: UserProfile): Boolean =
        webViewScreenIos?.syncUserProfile(profile) ?: false

    fun simulateError(): Boolean = webViewScreenIos?.simulateError() ?: false
}