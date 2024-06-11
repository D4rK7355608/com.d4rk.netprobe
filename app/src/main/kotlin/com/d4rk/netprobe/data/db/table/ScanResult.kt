package com.d4rk.netprobe.data.db.table

data class ScanResult(
    val ipAddress: String,
    val responseCode: String,
    val function: String,
    val responseTime: Long,
    val isCamera: Boolean,
    val serverCount: Int
)