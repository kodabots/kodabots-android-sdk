package ai.koda.mobile.sdk.core

import android.os.Build

inline fun <T> sdk33OrHigher(
    block: () -> T
) = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU)
    block() else null