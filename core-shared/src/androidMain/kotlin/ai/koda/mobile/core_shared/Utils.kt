package ai.koda.mobile.core_shared

import android.os.Build

inline fun <T> sdk33OrHigher(
    block: () -> T
) = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU)
    block() else null