package com.d4rk.netprobe.data.model.ui.screens

data class UiSpeedTestScreen(
    val inProgress: Boolean,
    val arcValue: Float,
    val speed: String,
    val ping: String,
    val wifiStrength: String,
    val maxSpeed: String
)