package com.pokevault.mobile.ui.feature.pickup.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

data class PickupLocation(
    val latitude: Double,
    val longitude: Double,
)

interface PickupLocationClient {
    fun hasLocationPermission(): Boolean
    suspend fun getCurrentLocation(): PickupLocation?
}

@Singleton
class AndroidPickupLocationClient @Inject constructor(
    @ApplicationContext private val context: Context,
) : PickupLocationClient {
    override fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    override suspend fun getCurrentLocation(): PickupLocation? {
        if (!hasLocationPermission()) return null

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val enabledProviders = locationManager.getProviders(true)
        if (enabledProviders.isEmpty()) return null

        val prioritizedProviders = buildList {
            if (enabledProviders.contains(LocationManager.GPS_PROVIDER)) {
                add(LocationManager.GPS_PROVIDER)
            }
            if (enabledProviders.contains(LocationManager.NETWORK_PROVIDER)) {
                add(LocationManager.NETWORK_PROVIDER)
            }
            enabledProviders
                .filterNot { it == LocationManager.GPS_PROVIDER || it == LocationManager.NETWORK_PROVIDER }
                .forEach(::add)
        }

        prioritizedProviders.forEach { provider ->
            val freshLocation = requestCurrentLocation(locationManager, provider)
            if (freshLocation != null) {
                return freshLocation
            }
        }

        return prioritizedProviders
            .asSequence()
            .mapNotNull { provider ->
                runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
            }
            .maxByOrNull(Location::getTime)
            ?.toPickupLocation()
    }

    private suspend fun requestCurrentLocation(
        locationManager: LocationManager,
        provider: String,
    ): PickupLocation? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null

        return suspendCancellableCoroutine { continuation ->
            runCatching {
                locationManager.getCurrentLocation(
                    provider,
                    null,
                    ContextCompat.getMainExecutor(context),
                ) { location ->
                    if (continuation.isActive) {
                        continuation.resume(location?.toPickupLocation())
                    }
                }
            }.onFailure {
                if (continuation.isActive) {
                    continuation.resume(null)
                }
            }
        }
    }

    private fun Location.toPickupLocation(): PickupLocation =
        PickupLocation(latitude = latitude, longitude = longitude)
}
