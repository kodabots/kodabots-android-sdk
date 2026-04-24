package ai.koda.mobile.core_shared.config

import ai.koda.mobile.core_shared.model.UserProfile
import cocoapods.lottie_ios.CompatibleAnimation
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIImage


class KodaBotsConfig(
    var userProfile: UserProfile = UserProfile(),
    var blockId: String? = null,
    var progressConfig: KodaBotsProgressConfig? = null,
    var timeoutConfig: KodaBotsTimedOutConfig? = null,
    var customClientToken: String? = null,
    var customBaseUrl: String? = null,
    var customBaseRestUrl: String? = null
)

class KodaBotsProgressConfig(
    var backgroundColor: UIColor? = null,
    var progressColor: UIColor? = null,
    var customAnimation: CustomAnimation? = null
)

class CustomAnimation(
    var name: String = "default_loader",
    var subdirectory: String? = null,
    var bundle: NSBundle = NSBundle.mainBundle
)

class KodaBotsTimedOutConfig {
    var timeout: Int? = null
    var image: UIImage? = null
    var backgroundColor: UIColor? = null
    var buttonText: String? = null
    var buttonColor: UIColor? = null
    var message: String? = null
    var messageTextColor: UIColor? = null
    var messageFont: UIFont? = null
}