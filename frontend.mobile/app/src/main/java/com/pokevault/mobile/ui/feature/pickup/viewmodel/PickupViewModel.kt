package com.pokevault.mobile.ui.feature.pickup.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokevault.mobile.ui.feature.pickup.location.PickupLocationClient
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val locationClient: PickupLocationClient,
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
                _uiState.update {
                    it.copy(
                        locationPermissionGranted = event.granted,
                        distanceInMeters = if (event.granted) it.distanceInMeters else null,
                    )
                }
                if (event.granted) {
                    refreshLocation()
                }
            }
            PickupEvent.OnGetDirectionsClick -> {
                viewModelScope.launch {
                    val state = _uiState.value
                    val originLocation = if (state.locationPermissionGranted) {
                        locationClient.getCurrentLocation()
                    } else {
                        null
                    }
                    val originParam = originLocation?.let { "&origin=${it.latitude},${it.longitude}" }.orEmpty()
                    val url = "https://www.google.com/maps/dir/?api=1$originParam&destination=${state.destinationLatitude},${state.destinationLongitude}&travelmode=walking"
                    _effects.send(PickupEffect.OpenDirections(url))
                }
            }
        }
    }

    fun checkPermissionAndLoadLocation() {
        val granted = locationClient.hasLocationPermission()
        _uiState.update { it.copy(locationPermissionGranted = granted) }
        if (granted) {
            refreshLocation()
        } else {
            _uiState.update { it.copy(distanceInMeters = null) }
        }
    }

    private fun refreshLocation() {
        viewModelScope.launch {
            val currentLocation = locationClient.getCurrentLocation()
            val distance = currentLocation?.let {
                val state = _uiState.value
                distanceInMeters(
                    it.latitude,
                    it.longitude,
                    state.destinationLatitude,
                    state.destinationLongitude,
                )
            }
            _uiState.update { it.copy(distanceInMeters = distance) }
        }
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
