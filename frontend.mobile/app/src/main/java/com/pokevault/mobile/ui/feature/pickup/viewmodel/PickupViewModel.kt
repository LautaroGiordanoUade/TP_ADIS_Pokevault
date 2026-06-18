package com.pokevault.mobile.ui.feature.pickup.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class PickupUiState(
    val locationPermissionGranted: Boolean = false,
    val distanceInMeters: Double? = null,
    val destinationLatitude: Double = -34.618279,
    val destinationLongitude: Double = -58.381565,
    val destinationAddress: String = "Lima 757, Ciudad Autonoma de Buenos Aires, Argentina",
)

sealed interface PickupEvent {
    data class OnPermissionResult(val granted: Boolean) : PickupEvent
    data object OnGetDirectionsClick : PickupEvent
}

sealed interface PickupEffect {
    data class OpenDirections(val url: String) : PickupEffect
}

@HiltViewModel
class PickupViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PickupUiState())
    val uiState = _uiState.asStateFlow()

    private val _effects = Channel<PickupEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        checkPermissionAndLoadLocation()
    }

    fun onEvent(event: PickupEvent) {
        when (event) {
            is PickupEvent.OnPermissionResult -> {
                _uiState.update { it.copy(locationPermissionGranted = event.granted) }
                if (event.granted) {
                    loadLocation()
                }
            }
            PickupEvent.OnGetDirectionsClick -> {
                viewModelScope.launch {
                    val state = _uiState.value
                    val originLocation = if (state.locationPermissionGranted) findLastKnownLocation() else null
                    val originParam = originLocation?.let { "&origin=${it.latitude},${it.longitude}" }.orEmpty()
                    val url = "https://www.google.com/maps/dir/?api=1$originParam&destination=${state.destinationLatitude},${state.destinationLongitude}&travelmode=walking"
                    _effects.send(PickupEffect.OpenDirections(url))
                }
            }
        }
    }

    fun checkPermissionAndLoadLocation() {
        val granted = hasLocationPermission()
        _uiState.update { it.copy(locationPermissionGranted = granted) }
        if (granted) {
            loadLocation()
        }
    }

    private fun loadLocation() {
        val lastLocation = findLastKnownLocation()
        if (lastLocation != null) {
            val state = _uiState.value
            val distance = distanceInMeters(
                lastLocation.latitude,
                lastLocation.longitude,
                state.destinationLatitude,
                state.destinationLongitude
            )
            _uiState.update { it.copy(distanceInMeters = distance) }
        }
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun findLastKnownLocation(): Location? {
        if (!hasLocationPermission()) return null
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return manager.getProviders(true)
            .mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull { it.time }
    }

    private fun distanceInMeters(fromLat: Double, fromLng: Double, toLat: Double, toLng: Double): Double {
        val earthRadius = 6_371_000.0
        val dLat = Math.toRadians(toLat - fromLat)
        val dLng = Math.toRadians(toLng - fromLng)
        val lat1 = Math.toRadians(fromLat)
        val lat2 = Math.toRadians(toLat)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1) * cos(lat2) * sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
}
