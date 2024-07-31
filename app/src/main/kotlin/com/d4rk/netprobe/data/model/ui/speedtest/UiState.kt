package com.d4rk.netprobe.data.model.ui.speedtest

data class UiState(
    val inProgress: Boolean,
    val arcValue: Float,
    val speed: String,
    val ping: String,
    val wifiStrength: String,
    val maxSpeed: String
)