package com.example.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.example.data.models.GpsLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocation || coarseLocation
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): GpsLocation? {
        if (!hasLocationPermission()) return null

        return try {
            withTimeoutOrNull(5000L) {
                val cts = CancellationTokenSource()
                val location: Location? = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cts.token
                ).await()

                location?.let {
                    GpsLocation(
                        latitude = it.latitude,
                        longitude = it.longitude,
                        accuracy = it.accuracy,
                        altitude = if (it.hasAltitude()) it.altitude else 0.0
                    )
                } ?: run {
                    // Fallback to last known location
                    val lastKnown: Location? = fusedLocationClient.lastLocation.await()
                    lastKnown?.let {
                        GpsLocation(
                            latitude = it.latitude,
                            longitude = it.longitude,
                            accuracy = it.accuracy,
                            altitude = if (it.hasAltitude()) it.altitude else 0.0
                        )
                    }
                }
            }
        } catch (_: Exception) {
            null
        }
    }
}
