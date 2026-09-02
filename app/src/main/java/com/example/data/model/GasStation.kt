package com.example.data.model

data class GasStation(
    val id: String,
    val name: String,
    val address: String,
    val postalCode: String,
    val municipality: String,
    val province: String,
    val islandId: String? = null,
    val islandName: String? = null,
    val schedule: String,
    val latitude: Double?,
    val longitude: Double?,
    val distanceKm: Double? = null,
    val prices: Map<FuelType, Double>,
    val is24h: Boolean,
    val brandCategory: BrandCategory,
    val isFavorite: Boolean = false
) {
    fun getPriceFor(fuelType: FuelType): Double? = prices[fuelType]

    fun getLowestPrice(): Double? = prices.values.minOrNull()
}

enum class BrandCategory(
    val displayName: String,
    val colorHex: Long,
    val isLowCost: Boolean,
    val assetFileName: String
) {
    REPSOL("Repsol", 0xFFFF5722, false, "repsol.png"),
    CEPSA("Cepsa / Moeve", 0xFFD32F2F, false, "cepsa.png"),
    BP("BP", 0xFF2E7D32, false, "bp.png"),
    SHELL("Shell", 0xFFFBC02D, false, "shell.png"),
    GALP("Galp", 0xFFE64A19, false, "galp.png"),
    PLENOIL("Plenoil", 0xFF0288D1, true, "plenoil.png"),
    PLENERGY("Plenergy", 0xFF0288D1, true, "plenergy.png"),
    BALLENOIL("Ballenoil", 0xFF1976D2, true, "ballenoil.png"),
    PETROPRIX("Petroprix", 0xFF7B1FA2, true, "petroprix.png"),
    ALCAMPO("Alcampo", 0xFFE53935, true, "alcampo.png"),
    CARREFOUR("Carrefour", 0xFF1565C0, true, "carrefour.png"),
    EROSKI("Eroski", 0xFFC2185B, true, "eroski.png"),
    CAMPSA("Campsa", 0xFF00796B, false, "campsa.png"),
    AVIA("Avia", 0xFFD81B60, false, "avia.png"),
    DISA("Disa", 0xFFF57C00, false, "disa.png"),
    CANARY_OIL("Canary Oil", 0xFFF57C00, false, "canary_oil.png"),
    H2EXAGON("H2EXAGON", 0xFF00ACC1, false, "h2exagon.png"),
    OCEANO("Océano", 0xFF039BE5, false, "oceano.png"),
    SANTANA("Santana Dominguez", 0xFFD32F2F, false, "santana.png"),
    OTHER("Gasolinera", 0xFF455A64, false, "other.png");

    companion object {
        fun fromRotulo(rotulo: String?): BrandCategory {
            val upper = rotulo?.uppercase() ?: return OTHER
            return when {
                upper.contains("REPSOL") -> REPSOL
                upper.contains("CEPSA") || upper.contains("MOEVE") -> CEPSA
                upper.contains("BP") -> BP
                upper.contains("SHELL") -> SHELL
                upper.contains("GALP") -> GALP
                upper.contains("PLENOIL") -> PLENOIL
                upper.contains("PLENERGY") -> PLENERGY
                upper.contains("BALLENOIL") -> BALLENOIL
                upper.contains("PETROPRIX") -> PETROPRIX
                upper.contains("ALCAMPO") -> ALCAMPO
                upper.contains("CARREFOUR") -> CARREFOUR
                upper.contains("EROSKI") -> EROSKI
                upper.contains("CAMPSA") -> CAMPSA
                upper.contains("AVIA") -> AVIA
                upper.contains("DISA") -> DISA
                upper.contains("CANARY OIL") || upper.contains("CANARYOIL") -> CANARY_OIL
                upper.contains("H2EXAGON") -> H2EXAGON
                upper.contains("OCEANO") || upper.contains("OCÉANO") -> OCEANO
                upper.contains("SANTANA") -> SANTANA
                else -> OTHER
            }
        }
    }
}

fun GasStationDto.toDomain(isFavorite: Boolean = false, userLat: Double? = null, userLng: Double? = null): GasStation {
    fun parsePrice(priceStr: String?): Double? {
        if (priceStr.isNullOrBlank()) return null
        return priceStr.replace(',', '.').trim().toDoubleOrNull()
    }

    val priceMap = mutableMapOf<FuelType, Double>()
    parsePrice(precioGasolina95E5)?.let { priceMap[FuelType.GASOLINA_95] = it }
    parsePrice(precioGasoleoA)?.let { priceMap[FuelType.DIESEL_A] = it }
    parsePrice(precioGasolina98E5)?.let { priceMap[FuelType.GASOLINA_98] = it }
    parsePrice(precioGasoleoPremium)?.let { priceMap[FuelType.DIESEL_PREMIUM] = it }
    parsePrice(precioGLP)?.let { priceMap[FuelType.GLP] = it }
    parsePrice(precioGNC)?.let { priceMap[FuelType.GNC] = it }
    parsePrice(precioGasoleoB)?.let { priceMap[FuelType.DIESEL_B] = it }

    val lat = latitud?.replace(',', '.')?.toDoubleOrNull()
    val lng = longitud?.replace(',', '.')?.toDoubleOrNull()

    val dist = if (userLat != null && userLng != null && lat != null && lng != null) {
        calculateDistanceKm(userLat, userLng, lat, lng)
    } else null

    val scheduleStr = horario ?: "Horario no disponible"
    val is24 = scheduleStr.contains("24H", ignoreCase = true) || 
               scheduleStr.contains("L-D: 24", ignoreCase = true) ||
               scheduleStr.contains("24 horas", ignoreCase = true)

    val cleanName = (rotulo ?: "Gasolinera").trim().let {
        if (it.isBlank()) "Gasolinera" else it
    }

    val detectedIsland = SpanishProvinces.detectIsland(
        postalCode = codigoPostal ?: "",
        municipality = municipio ?: localidad ?: "",
        provinceId = idProvincia ?: ""
    )

    return GasStation(
        id = id ?: "${cleanName}_${direccion.orEmpty()}",
        name = cleanName,
        address = (direccion ?: "").trim(),
        postalCode = (codigoPostal ?: "").trim(),
        municipality = (municipio ?: localidad ?: "").trim(),
        province = (provincia ?: "").trim(),
        islandId = detectedIsland?.id,
        islandName = detectedIsland?.name,
        schedule = scheduleStr.trim(),
        latitude = lat,
        longitude = lng,
        distanceKm = dist,
        prices = priceMap,
        is24h = is24,
        brandCategory = BrandCategory.fromRotulo(cleanName),
        isFavorite = isFavorite
    )
}

fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0 // Radius of the earth in km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return r * c
}
