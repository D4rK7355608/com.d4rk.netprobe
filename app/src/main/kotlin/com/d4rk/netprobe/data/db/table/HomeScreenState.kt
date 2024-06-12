package com.d4rk.netprobe.data.db.table

data class HomeScreenState(
    val ipAddress : String = "" ,
    val scanResult : ScanResult? = null ,
    val isLoading : Boolean = false
)