package com.pokevault.mobile.ui.feature.pickup.qr

object QrScanContract {
    const val ResultKey = "pickup_qr_scan_result"
}

data class PickupQrScanResult(
    val rawValue: String,
    val normalizedValue: String,
    val isAuthorized: Boolean,
)
