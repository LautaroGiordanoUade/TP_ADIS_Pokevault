package com.pokevault.mobile.ui.feature.pickup.viewmodel

import com.pokevault.mobile.MainDispatcherRule
import com.pokevault.mobile.ui.feature.pickup.location.PickupLocation
import com.pokevault.mobile.ui.feature.pickup.location.PickupLocationClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PickupViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state loads distance when permission is already granted`() = runTest {
        val locationClient = FakePickupLocationClient(
            permissionGranted = true,
            currentLocation = PickupLocation(latitude = -34.6175, longitude = -58.3810),
        )

        val viewModel = PickupViewModel(locationClient)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.locationPermissionGranted)
        assertNotNull(state.distanceInMeters)
        assertTrue(state.distanceInMeters!! > 0)
    }

    @Test
    fun `initial state keeps distance null when permission is denied`() = runTest {
        val viewModel = PickupViewModel(
            FakePickupLocationClient(permissionGranted = false, currentLocation = PickupLocation(-34.6175, -58.3810)),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.locationPermissionGranted)
        assertNull(state.distanceInMeters)
    }

    @Test
    fun `permission granted with unavailable location keeps pending distance`() = runTest {
        val locationClient = FakePickupLocationClient(permissionGranted = true, currentLocation = null)

        val viewModel = PickupViewModel(locationClient)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.locationPermissionGranted)
        assertNull(state.distanceInMeters)
    }

    @Test
    fun `granting permission later refreshes location`() = runTest {
        val locationClient = FakePickupLocationClient(permissionGranted = false, currentLocation = null)
        val viewModel = PickupViewModel(locationClient)
        advanceUntilIdle()

        locationClient.permissionGranted = true
        locationClient.currentLocation = PickupLocation(latitude = -34.6160, longitude = -58.3805)

        viewModel.onEvent(PickupEvent.OnPermissionResult(granted = true))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.locationPermissionGranted)
        assertNotNull(state.distanceInMeters)
    }

    @Test
    fun `directions omit origin when permission is denied`() = runTest {
        val viewModel = PickupViewModel(
            FakePickupLocationClient(permissionGranted = false, currentLocation = PickupLocation(-34.6175, -58.3810)),
        )
        advanceUntilIdle()

        val effectDeferred = async { viewModel.effects.first() }
        viewModel.onEvent(PickupEvent.OnGetDirectionsClick)
        advanceUntilIdle()

        val effect = effectDeferred.await() as PickupEffect.OpenDirections
        assertFalse(effect.url.contains("&origin="))
    }

    @Test
    fun `directions include origin when location is available`() = runTest {
        val viewModel = PickupViewModel(
            FakePickupLocationClient(
                permissionGranted = true,
                currentLocation = PickupLocation(latitude = -34.6175, longitude = -58.3810),
            ),
        )
        advanceUntilIdle()

        val effectDeferred = async { viewModel.effects.first() }
        viewModel.onEvent(PickupEvent.OnGetDirectionsClick)
        advanceUntilIdle()

        val effect = effectDeferred.await() as PickupEffect.OpenDirections
        assertTrue(effect.url.contains("&origin=-34.6175,-58.381"))
        assertTrue(effect.url.contains("destination=-34.618279,-58.381565"))
    }

    @Test
    fun `denying permission clears previous distance`() = runTest {
        val locationClient = FakePickupLocationClient(
            permissionGranted = true,
            currentLocation = PickupLocation(latitude = -34.6175, longitude = -58.3810),
        )
        val viewModel = PickupViewModel(locationClient)
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.distanceInMeters)

        viewModel.onEvent(PickupEvent.OnPermissionResult(granted = false))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.locationPermissionGranted)
        assertNull(viewModel.uiState.value.distanceInMeters)
    }

    private class FakePickupLocationClient(
        var permissionGranted: Boolean,
        var currentLocation: PickupLocation?,
    ) : PickupLocationClient {
        override fun hasLocationPermission(): Boolean = permissionGranted

        override suspend fun getCurrentLocation(): PickupLocation? = currentLocation
    }
}
