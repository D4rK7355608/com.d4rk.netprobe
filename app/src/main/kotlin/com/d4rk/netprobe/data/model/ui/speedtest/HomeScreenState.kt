package com.d4rk.netprobe.data.model.ui.speedtest

import com.d4rk.netprobe.data.model.ui.ipscan.ScanResult

data class HomeScreenState(
    val ipAddress : String = "",
    val scanResult : ScanResult? = null,
    val isLoading : Boolean = false
)