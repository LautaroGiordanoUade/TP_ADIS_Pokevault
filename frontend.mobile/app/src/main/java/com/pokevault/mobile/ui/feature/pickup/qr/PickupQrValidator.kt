package com.pokevault.mobile.ui.feature.pickup.qr

object PickupQrValidator {
    fun validate(rawValue: String, expectedOrderCode: String): PickupQrScanResult {
        val normalizedRawValue = rawValue.trim()
        val normalizedExpectedOrderCode = expectedOrderCode.trim().uppercase()
        val acceptedValues = setOf(
            normalizedExpectedOrderCode,
            "PICKUP:$normalizedExpectedOrderCode",
            "POKEVAULT:PICKUP:$normalizedExpectedOrderCode",
        )

        val normalizedComparisonValue = normalizedRawValue.uppercase()
        return PickupQrScanResult(
            rawValue = normalizedRawValue,
            normalizedValue = normalizedComparisonValue,
            isAuthorized = normalizedComparisonValue in acceptedValues,
        )
    }
}
