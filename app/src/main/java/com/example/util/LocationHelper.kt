package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.data.model.Province
import com.example.data.model.SpanishIsland
import com.example.data.model.SpanishProvinces
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    @SuppressLint("MissingPermission")
    suspend fun getLastKnownLocation(context: Context): Location? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) return@withContext null
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return@withContext null

        val providers = locationManager.getProviders(true)
        var bestLocation: Location? = null

        for (provider in providers) {
            val l = locationManager.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                bestLocation = l
            }
        }
        bestLocation
    }

    @SuppressLint("MissingPermission")
    suspend fun requestSingleLocationUpdate(context: Context, timeoutMs: Long = 6000L): Location? {
        val cached = getLastKnownLocation(context)
        // If cached location is recent (less than 15 minutes old), use it
        if (cached != null && (System.currentTimeMillis() - cached.time) < 15 * 60 * 1000) {
            return cached
        }

        if (!hasLocationPermission(context)) return cached

        return withTimeoutOrNull(timeoutMs) {
            suspendCancellableCoroutine { continuation ->
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                if (locationManager == null) {
                    continuation.resume(cached)
                    return@suspendCancellableCoroutine
                }

                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        try {
                            locationManager.removeUpdates(this)
                        } catch (_: Exception) {}
                        if (continuation.isActive) {
                            continuation.resume(location)
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }

                try {
                    val provider = when {
                        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                        else -> LocationManager.PASSIVE_PROVIDER
                    }

                    locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                } catch (e: Exception) {
                    if (continuation.isActive) continuation.resume(cached)
                }

                continuation.invokeOnCancellation {
                    try {
                        locationManager.removeUpdates(listener)
                    } catch (_: Exception) {}
                }
            }
        } ?: cached
    }

    suspend fun resolveProvinceAndIsland(
        context: Context,
        location: Location
    ): Pair<Province, SpanishIsland?> = withContext(Dispatchers.IO) {
        val lat = location.latitude
        val lng = location.longitude

        // 1. Try Geocoder for accurate postal code / municipality / province
        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale("es", "ES"))
                val addresses: List<Address>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { cont ->
                        geocoder.getFromLocation(lat, lng, 1) { addrs ->
                            cont.resume(addrs)
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(lat, lng, 1)
                }

                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val postalCode = addr.postalCode ?: ""
                    val locality = addr.locality ?: addr.subLocality ?: ""
                    val adminArea = addr.adminArea ?: addr.subAdminArea ?: ""

                    // If we have postal code, match province from postal code prefix
                    if (postalCode.length >= 2) {
                        val prov = SpanishProvinces.findProvinceByPostalCode(postalCode)
                        if (prov != null) {
                            val island = SpanishProvinces.detectIsland(
                                postalCode = postalCode,
                                municipality = locality,
                                provinceId = prov.id
                            )
                            return@withContext Pair(prov, island)
                        }
                    }

                    // Match province by adminArea name
                    if (adminArea.isNotBlank()) {
                        val prov = SpanishProvinces.findProvinceByName(adminArea)
                        if (prov != null) {
                            val island = SpanishProvinces.detectIsland(
                                postalCode = postalCode,
                                municipality = locality,
                                provinceId = prov.id
                            )
                            return@withContext Pair(prov, island)
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Geocoder failure fallback to geometric coordinate matching
        }

        // 2. Fallback to geometric coordinate matching
        SpanishProvinces.findNearestProvinceAndIsland(lat, lng)
    }
}
