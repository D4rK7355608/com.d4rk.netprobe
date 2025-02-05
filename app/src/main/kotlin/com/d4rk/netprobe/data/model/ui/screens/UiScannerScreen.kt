package com.d4rk.netprobe.data.model.ui.screens

data class UiScannerScreen(
    val ipAddress : String = "" ,
    val scanResult : ScanResult? = null ,
    val isLoading : Boolean = false
)