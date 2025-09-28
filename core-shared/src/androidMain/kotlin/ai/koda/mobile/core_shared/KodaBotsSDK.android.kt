package ai.koda.mobile.core_shared

import ai.koda.mobile.core_shared.presentation.KodaBotsWebViewFragment
import ai.koda.mobile.core_shared.screen.KodaBotsWebViewScreen

actual fun generateScreen(): KodaBotsWebViewScreen {
    return KodaBotsWebViewFragment()
}
