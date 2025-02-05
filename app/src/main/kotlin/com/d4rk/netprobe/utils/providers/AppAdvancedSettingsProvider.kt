package com.d4rk.netprobe.utils.providers

import com.d4rk.android.libs.apptoolkit.utils.interfaces.providers.AdvancedSettingsProvider
import com.d4rk.netprobe.data.core.AppCoreManager

class AppAdvancedSettingsProvider : AdvancedSettingsProvider {
    override val bugReportUrl: String
        get() = "https://github.com/D4rK7355608/${AppCoreManager.instance.packageName}/issues/new"
}