package com.example.data.network

import android.content.Context
import android.location.Address
import android.location.Location
import com.example.data.model.Province
import com.example.data.model.SpanishIsland
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ApiCallLog(
    val timestamp: Long,
    val timeFormatted: String,
    val endpoint: String,
    val isNetwork: Boolean,
    val isCache: Boolean,
    val statusCode: Int,
    val latencyMs: Long,
    val isSuccess: Boolean,
    val errorMessage: String? = null
)

data class MitecoApiStats(
    val totalRequests: Int = 0,
    val networkRequests: Int = 0,
    val cacheHits: Int = 0,
    val errors: Int = 0,
    val lastLatencyMs: Long = 0,
    val lastRequestTimestamp: Long? = null,
    val lastRequestTimeFormatted: String? = null,
    val lastEndpoint: String? = null,
    val lastStatusCode: Int? = null,
    val lastErrorMessage: String? = null,
    val todayNetworkCount8to20: Int = 0,
    val hasMorning6Fetch: Boolean = false,
    val hasEvening18Fetch: Boolean = false,
    val cacheSizeBytes: Long = 0,
    val recentCalls: List<ApiCallLog> = emptyList()
)

data class GpsLocationStats(
    val totalGpsRequests: Int = 0,
    val lastLocationTimestamp: Long? = null,
    val lastLocationTimeFormatted: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Float? = null,
    val altitudeMeters: Double? = null,
    val provider: String? = null,
    val fullAddress: String? = null,
    val street: String? = null,
    val postalCode: String? = null,
    val locality: String? = null,
    val adminArea: String? = null,
    val country: String? = null,
    val detectedZoneName: String? = null,
    val isGeocoderSuccess: Boolean = false,
    val detectionMethod: String = "Pendiente de fijación"
)

object ApiStatsTracker {

    private val _mitecoStats = MutableStateFlow(MitecoApiStats())
    val mitecoStats: StateFlow<MitecoApiStats> = _mitecoStats.asStateFlow()

    private val _gpsStats = MutableStateFlow(GpsLocationStats())
    val gpsStats: StateFlow<GpsLocationStats> = _gpsStats.asStateFlow()

    private val timeFormatter = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())

    fun recordMitecoRequest(
        endpoint: String,
        isNetwork: Boolean,
        isCache: Boolean,
        statusCode: Int,
        latencyMs: Long,
        isSuccess: Boolean,
        errorMessage: String? = null,
        context: Context? = null
    ) {
        val now = System.currentTimeMillis()
        val timeStr = timeFormatter.format(Date(now))

        val log = ApiCallLog(
            timestamp = now,
            timeFormatted = timeStr,
            endpoint = endpoint,
            isNetwork = isNetwork,
            isCache = isCache,
            statusCode = statusCode,
            latencyMs = latencyMs,
            isSuccess = isSuccess,
            errorMessage = errorMessage
        )

        // Read cache size if context available
        var cacheSize = _mitecoStats.value.cacheSizeBytes
        var count820 = _mitecoStats.value.todayNetworkCount8to20
        var has6 = _mitecoStats.value.hasMorning6Fetch
        var has18 = _mitecoStats.value.hasEvening18Fetch

        if (context != null) {
            try {
                val prefs = context.getSharedPreferences("api_policy", Context.MODE_PRIVATE)
                count820 = prefs.getInt("count_8_20", count820)
                has6 = prefs.getBoolean("has_6_fetch", has6)
                has18 = prefs.getBoolean("has_18_fetch", has18)

                val cacheDir = File(context.cacheDir, "miteco_cache")
                if (cacheDir.exists()) {
                    cacheSize = getFolderSize(cacheDir)
                }
            } catch (_: Exception) {}
        }

        _mitecoStats.update { current ->
            val updatedLogs = (listOf(log) + current.recentCalls).take(15)
            current.copy(
                totalRequests = current.totalRequests + 1,
                networkRequests = if (isNetwork) current.networkRequests + 1 else current.networkRequests,
                cacheHits = if (isCache) current.cacheHits + 1 else current.cacheHits,
                errors = if (!isSuccess) current.errors + 1 else current.errors,
                lastLatencyMs = latencyMs,
                lastRequestTimestamp = now,
                lastRequestTimeFormatted = timeStr,
                lastEndpoint = endpoint,
                lastStatusCode = statusCode,
                lastErrorMessage = errorMessage,
                todayNetworkCount8to20 = count820,
                hasMorning6Fetch = has6,
                hasEvening18Fetch = has18,
                cacheSizeBytes = cacheSize,
                recentCalls = updatedLogs
            )
        }
    }

    fun recordGpsLocation(
        location: Location,
        address: Address?,
        resolvedProvince: Province?,
        resolvedIsland: SpanishIsland?,
        wasGeocoderSuccess: Boolean
    ) {
        val now = System.currentTimeMillis()
        val timeStr = timeFormatter.format(Date(now))

        val streetStr = when {
            address?.thoroughfare != null && address.subThoroughfare != null ->
                "${address.thoroughfare}, ${address.subThoroughfare}"
            address?.thoroughfare != null -> address.thoroughfare
            else -> null
        }

        val fullAddrStr = when {
            address != null && !address.getAddressLine(0).isNullOrBlank() -> address.getAddressLine(0)
            address != null -> {
                listOfNotNull(streetStr, address.postalCode, address.locality, address.adminArea, address.countryName)
                    .joinToString(", ")
            }
            resolvedProvince != null -> {
                val islandName = resolvedIsland?.name
                if (islandName != null) "$islandName (${resolvedProvince.name}), España"
                else "${resolvedProvince.name}, España"
            }
            else -> "Coordenadas: ${String.format(Locale.US, "%.5f", location.latitude)}, ${String.format(Locale.US, "%.5f", location.longitude)}"
        }

        val zoneName = when {
            resolvedIsland != null -> "${resolvedIsland.name} (${resolvedProvince?.name ?: ""})"
            resolvedProvince != null -> resolvedProvince.name
            else -> "Zona desconocida"
        }

        val methodStr = if (wasGeocoderSuccess) "GPS + Geocodificador Android" else "Fijación GPS + Detección Geométrica"

        _gpsStats.update { current ->
            current.copy(
                totalGpsRequests = current.totalGpsRequests + 1,
                lastLocationTimestamp = now,
                lastLocationTimeFormatted = timeStr,
                latitude = location.latitude,
                longitude = location.longitude,
                accuracyMeters = location.accuracy,
                altitudeMeters = if (location.hasAltitude()) location.altitude else null,
                provider = location.provider ?: "gps",
                fullAddress = fullAddrStr,
                street = streetStr,
                postalCode = address?.postalCode,
                locality = address?.locality ?: address?.subLocality,
                adminArea = address?.adminArea ?: resolvedProvince?.name,
                country = address?.countryName ?: "España",
                detectedZoneName = zoneName,
                isGeocoderSuccess = wasGeocoderSuccess,
                detectionMethod = methodStr
            )
        }
    }

    fun syncPolicyFromPrefs(context: Context) {
        try {
            val prefs = context.getSharedPreferences("api_policy", Context.MODE_PRIVATE)
            val count820 = prefs.getInt("count_8_20", 0)
            val has6 = prefs.getBoolean("has_6_fetch", false)
            val has18 = prefs.getBoolean("has_18_fetch", false)
            val cacheDir = File(context.cacheDir, "miteco_cache")
            val cacheSize = if (cacheDir.exists()) getFolderSize(cacheDir) else 0L

            _mitecoStats.update {
                it.copy(
                    todayNetworkCount8to20 = count820,
                    hasMorning6Fetch = has6,
                    hasEvening18Fetch = has18,
                    cacheSizeBytes = cacheSize
                )
            }
        } catch (_: Exception) {}
    }

    private fun getFolderSize(dir: File): Long {
        var size = 0L
        try {
            val files = dir.listFiles() ?: return 0L
            for (f in files) {
                size += if (f.isDirectory) getFolderSize(f) else f.length()
            }
        } catch (_: Exception) {}
        return size
    }
}
