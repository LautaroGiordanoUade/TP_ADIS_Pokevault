package com.pokevault.mobile.ui.feature.pickup.qr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PickupQrValidatorTest {
    @Test
    fun `authorizes plain order code ignoring case and surrounding spaces`() {
        val result = PickupQrValidator.validate(
            rawValue = "  pkm-a7x2  ",
            expectedOrderCode = "PKM-A7X2",
        )

        assertTrue(result.isAuthorized)
        assertEquals("pkm-a7x2", result.rawValue)
        assertEquals("PKM-A7X2", result.normalizedValue)
    }

    @Test
    fun `authorizes supported pickup prefixes`() {
        val shortPrefix = PickupQrValidator.validate("PICKUP:PKM-A7X2", "PKM-A7X2")
        val fullPrefix = PickupQrValidator.validate("POKEVAULT:PICKUP:PKM-A7X2", "PKM-A7X2")

        assertTrue(shortPrefix.isAuthorized)
        assertTrue(fullPrefix.isAuthorized)
    }

    @Test
    fun `rejects qr from another order`() {
        val result = PickupQrValidator.validate(
            rawValue = "PICKUP:PKM-OTHER",
            expectedOrderCode = "PKM-A7X2",
        )

        assertFalse(result.isAuthorized)
    }
}
