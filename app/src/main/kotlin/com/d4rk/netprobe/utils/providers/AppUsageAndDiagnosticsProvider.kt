package com.d4rk.netprobe.utils.providers

import com.d4rk.android.libs.apptoolkit.utils.interfaces.providers.UsageAndDiagnosticsSettingsProvider
import com.d4rk.netprobe.BuildConfig

class AppUsageAndDiagnosticsProvider : UsageAndDiagnosticsSettingsProvider {

    override val isDebugBuild : Boolean
        get() {
            return BuildConfig.DEBUG
        }
}