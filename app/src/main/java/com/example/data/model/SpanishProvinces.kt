package com.example.data.model

data class Province(
    val id: String,
    val name: String,
    val ccaa: String,
    val defaultLat: Double,
    val defaultLng: Double,
    val isArchipelago: Boolean = false
)

data class SpanishIsland(
    val id: String,
    val name: String,
    val provinceId: String,
    val ccaa: String,
    val defaultLat: Double,
    val defaultLng: Double,
    val postalCodePrefixes: List<String> = emptyList(),
    val municipalities: List<String> = emptyList()
)

object SpanishProvinces {
    val islands: List<SpanishIsland> = listOf(
        // Canarias - Las Palmas (35)
        SpanishIsland(
            id = "gran_canaria",
            name = "Gran Canaria",
            provinceId = "35",
            ccaa = "Canarias",
            defaultLat = 27.9202,
            defaultLng = -15.5474,
            postalCodePrefixes = listOf("350", "351", "352", "353", "354"),
            municipalities = listOf(
                "Las Palmas de Gran Canaria", "Telde", "Santa Lucía de Tirajana",
                "San Bartolomé de Tirajana", "Arucas", "Ingenio", "Agüimes",
                "Gáldar", "Mogán", "Santa Brígida", "Santa María de Guía",
                "Teror", "Valsequillo", "La Aldea de San Nicolás", "Moya",
                "Vega de San Mateo", "Firgas", "Agaete", "Valleseco", "Tejeda", "Artenara"
            )
        ),
        SpanishIsland(
            id = "lanzarote",
            name = "Lanzarote y La Graciosa",
            provinceId = "35",
            ccaa = "Canarias",
            defaultLat = 29.0469,
            defaultLng = -13.6339,
            postalCodePrefixes = listOf("355"),
            municipalities = listOf("Arrecife", "Teguise", "Tías", "San Bartolomé", "Yaiza", "Tinajo", "Haría")
        ),
        SpanishIsland(
            id = "fuerteventura",
            name = "Fuerteventura",
            provinceId = "35",
            ccaa = "Canarias",
            defaultLat = 28.3587,
            defaultLng = -14.0537,
            postalCodePrefixes = listOf("356"),
            municipalities = listOf("Puerto del Rosario", "La Oliva", "Pájara", "Tuineje", "Antigua", "Betancuria")
        ),

        // Canarias - Santa Cruz de Tenerife (38)
        SpanishIsland(
            id = "tenerife",
            name = "Tenerife",
            provinceId = "38",
            ccaa = "Canarias",
            defaultLat = 28.2916,
            defaultLng = -16.6291,
            postalCodePrefixes = listOf("380", "381", "382", "383", "384", "385", "386"),
            municipalities = listOf(
                "Santa Cruz de Tenerife", "San Cristóbal de La Laguna", "Arona", "Adeje",
                "Granadilla de Abona", "La Orotava", "Los Realejos", "Puerto de la Cruz",
                "Candelaria", "Tacoronte", "Icod de los Vinos", "Guía de Isora",
                "San Miguel de Abona", "Güímar", "El Rosario", "Santa Úrsula",
                "Santiago del Teide", "Tegueste", "El Sauzal", "La Victoria de Acentejo",
                "San Juan de la Rambla", "Arico", "La Matanza de Acentejo", "Buenavista del Norte",
                "Los Silos", "Fasnia", "Vilaflor", "Garachico", "El Tanque"
            )
        ),
        SpanishIsland(
            id = "la_palma",
            name = "La Palma",
            provinceId = "38",
            ccaa = "Canarias",
            defaultLat = 28.6835,
            defaultLng = -17.7642,
            postalCodePrefixes = listOf("387"),
            municipalities = listOf(
                "Santa Cruz de La Palma", "Los Llanos de Aridane", "El Paso", "Breña Alta",
                "Breña Baja", "Tazacorte", "Villa de Mazo", "San Andrés y Sauces",
                "Puntallana", "Barlovento", "Puntagorda", "Tijarafe", "Fuencaliente de La Palma", "Garafía"
            )
        ),
        SpanishIsland(
            id = "la_gomera",
            name = "La Gomera",
            provinceId = "38",
            ccaa = "Canarias",
            defaultLat = 28.1130,
            defaultLng = -17.2241,
            postalCodePrefixes = listOf("388"),
            municipalities = listOf("San Sebastián de La Gomera", "Valle Gran Rey", "Alajeró", "Hermigua", "Vallehermoso", "Agulo")
        ),
        SpanishIsland(
            id = "el_hierro",
            name = "El Hierro",
            provinceId = "38",
            ccaa = "Canarias",
            defaultLat = 27.7500,
            defaultLng = -18.0200,
            postalCodePrefixes = listOf("389"),
            municipalities = listOf("Valverde", "La Frontera", "El Pinar de El Hierro")
        ),

        // Illes Balears (07)
        SpanishIsland(
            id = "mallorca",
            name = "Mallorca",
            provinceId = "07",
            ccaa = "Illes Balears",
            defaultLat = 39.6953,
            defaultLng = 3.0176,
            postalCodePrefixes = listOf("070", "071", "072", "073", "074", "075", "076"),
            municipalities = listOf(
                "Palma", "Calvià", "Manacor", "Marratxí", "Llucmajor", "Inca",
                "Alcúdia", "Felanitx", "Pollença", "Sóller", "Sa Pobla", "Santanyí",
                "Son Servera", "Andratx", "Capdepera", "Santa Margalida", "Campos",
                "Binissalem", "Artà", "Algaida", "Porreres", "Esporles", "Alaró",
                "Ses Salines", "Sineu", "Santa María del Camí", "Bunyola", "Sencelles",
                "Montuïri", "Campanet", "Llubí", "Lloseta", "Vilafranca de Bonany",
                "Maria de la Salut", "Mancor de la Vall", "Costitx", "Deià", "Petra",
                "Valldemossa", "Ariany", "Fornalutx", "Búger", "Lloret de Vistalegre",
                "Estellencs", "Banyalbufar", "Escorca"
            )
        ),
        SpanishIsland(
            id = "menorca",
            name = "Menorca",
            provinceId = "07",
            ccaa = "Illes Balears",
            defaultLat = 39.9496,
            defaultLng = 4.1105,
            postalCodePrefixes = listOf("077"),
            municipalities = listOf("Maó", "Mahon", "Ciutadella de Menorca", "Alaior", "Es Castell", "Ferreries", "Es Mercadal", "Sant Lluís", "Es Migjorn Gran")
        ),
        SpanishIsland(
            id = "ibiza",
            name = "Ibiza / Eivissa",
            provinceId = "07",
            ccaa = "Illes Balears",
            defaultLat = 38.9800,
            defaultLng = 1.4300,
            postalCodePrefixes = listOf("0780", "0781", "0782", "0783", "0784", "0785", "0788", "0789"),
            municipalities = listOf("Eivissa", "Ibiza", "Santa Eulària des Riu", "Sant Josep de sa Talaia", "Sant Antoni de Portmany", "Sant Joan de Labritja")
        ),
        SpanishIsland(
            id = "formentera",
            name = "Formentera",
            provinceId = "07",
            ccaa = "Illes Balears",
            defaultLat = 38.7055,
            defaultLng = 1.4336,
            postalCodePrefixes = listOf("0786", "0787"),
            municipalities = listOf("Formentera")
        )
    )

    val list: List<Province> = listOf(
        Province("01", "Álava", "País Vasco", 42.85, -2.67),
        Province("02", "Albacete", "Castilla-La Mancha", 38.99, -1.86),
        Province("03", "Alicante / Alacant", "Comunitat Valenciana", 38.35, -0.48),
        Province("04", "Almería", "Andalucía", 36.84, -2.46),
        Province("33", "Asturias", "Principado de Asturias", 43.36, -5.84),
        Province("05", "Ávila", "Castilla y León", 40.66, -4.70),
        Province("06", "Badajoz", "Extremadura", 38.88, -6.97),
        Province("07", "Balears (Illes)", "Illes Balears", 39.57, 2.65, isArchipelago = true),
        Province("08", "Barcelona", "Cataluña", 41.39, 2.17),
        Province("09", "Burgos", "Castilla y León", 42.34, -3.70),
        Province("10", "Cáceres", "Extremadura", 39.48, -6.37),
        Province("11", "Cádiz", "Andalucía", 36.53, -6.29),
        Province("39", "Cantabria", "Cantabria", 43.46, -3.80),
        Province("12", "Castellón / Castelló", "Comunitat Valenciana", 39.99, -0.05),
        Province("51", "Ceuta", "Ceuta", 35.89, -5.32),
        Province("13", "Ciudad Real", "Castilla-La Mancha", 38.99, -3.93),
        Province("14", "Córdoba", "Andalucía", 37.89, -4.78),
        Province("15", "Coruña (A)", "Galicia", 43.36, -8.41),
        Province("16", "Cuenca", "Castilla-La Mancha", 40.07, -2.14),
        Province("17", "Girona", "Cataluña", 41.98, 2.82),
        Province("18", "Granada", "Andalucía", 37.18, -3.60),
        Province("19", "Guadalajara", "Castilla-La Mancha", 40.63, -3.17),
        Province("20", "Gipuzkoa", "País Vasco", 43.32, -1.98),
        Province("21", "Huelva", "Andalucía", 37.26, -6.94),
        Province("22", "Huesca", "Aragón", 42.14, -0.41),
        Province("23", "Jaén", "Andalucía", 37.78, -3.79),
        Province("24", "León", "Castilla y León", 42.60, -5.57),
        Province("25", "Lleida", "Cataluña", 41.62, 0.62),
        Province("27", "Lugo", "Galicia", 43.01, -7.56),
        Province("28", "Madrid", "Comunidad de Madrid", 40.42, -3.70),
        Province("29", "Málaga", "Andalucía", 36.72, -4.42),
        Province("52", "Melilla", "Melilla", 35.29, -2.94),
        Province("30", "Murcia", "Región de Murcia", 37.99, -1.13),
        Province("31", "Navarra", "Comunidad Foral de Navarra", 42.82, -1.64),
        Province("32", "Ourense", "Galicia", 42.34, -7.86),
        Province("34", "Palencia", "Castilla y León", 42.01, -4.53),
        Province("35", "Palmas (Las)", "Canarias", 28.12, -15.43, isArchipelago = true),
        Province("36", "Pontevedra", "Galicia", 42.43, -8.64),
        Province("26", "Rioja (La)", "La Rioja", 42.47, -2.45),
        Province("37", "Salamanca", "Castilla y León", 40.97, -5.66),
        Province("38", "Santa Cruz de Tenerife", "Canarias", 28.47, -16.25, isArchipelago = true),
        Province("40", "Segovia", "Castilla y León", 40.95, -4.12),
        Province("41", "Sevilla", "Andalucía", 37.39, -5.98),
        Province("42", "Soria", "Castilla y León", 41.76, -2.47),
        Province("43", "Tarragona", "Cataluña", 41.12, 1.24),
        Province("44", "Teruel", "Aragón", 40.34, -1.11),
        Province("45", "Toledo", "Castilla-La Mancha", 39.86, -4.02),
        Province("46", "Valencia / València", "Comunitat Valenciana", 39.47, -0.38),
        Province("47", "Valladolid", "Castilla y León", 41.65, -4.72),
        Province("48", "Bizkaia", "País Vasco", 43.26, -2.93),
        Province("49", "Zamora", "Castilla y León", 41.50, -5.74),
        Province("50", "Zaragoza", "Aragón", 41.65, -0.88)
    )

    val defaultProvince = list.first { it.id == "28" } // Madrid

    fun findById(id: String): Province {
        return list.find { it.id == id } ?: defaultProvince
    }

    fun isArchipelagoProvince(provinceId: String): Boolean {
        return provinceId in listOf("07", "35", "38")
    }

    fun isArchipelagoCommunity(ccaa: String): Boolean {
        return ccaa.equals("Canarias", ignoreCase = true) || ccaa.equals("Illes Balears", ignoreCase = true)
    }

    fun getIslandsForProvince(provinceId: String): List<SpanishIsland> {
        return islands.filter { it.provinceId == provinceId }
    }

    fun getIslandsForCommunity(ccaa: String): List<SpanishIsland> {
        return islands.filter { it.ccaa.equals(ccaa, ignoreCase = true) }
    }

    fun findIslandById(islandId: String?): SpanishIsland? {
        if (islandId.isNullOrBlank()) return null
        return islands.find { it.id.equals(islandId, ignoreCase = true) }
    }

    fun detectIsland(
        postalCode: String,
        municipality: String = "",
        provinceId: String = ""
    ): SpanishIsland? {
        val cleanCp = postalCode.trim()
        val cleanMun = municipality.trim().lowercase()

        // Match by postal code prefix
        if (cleanCp.isNotEmpty()) {
            for (island in islands) {
                if (provinceId.isNotBlank() && island.provinceId != provinceId) continue
                if (island.postalCodePrefixes.any { cleanCp.startsWith(it) }) {
                    return island
                }
            }
        }

        // Match by municipality
        if (cleanMun.isNotEmpty()) {
            for (island in islands) {
                if (provinceId.isNotBlank() && island.provinceId != provinceId) continue
                if (island.municipalities.any { cleanMun.contains(it.lowercase()) } ||
                    cleanMun.contains(island.name.lowercase())) {
                    return island
                }
            }
        }

        // Fallback by provinceId if provinceId is 07 (Mallorca by default), 35 (Gran Canaria by default), 38 (Tenerife by default)
        return when (provinceId) {
            "07" -> islands.find { it.id == "mallorca" }
            "35" -> islands.find { it.id == "gran_canaria" }
            "38" -> islands.find { it.id == "tenerife" }
            else -> null
        }
    }

    fun findProvinceByPostalCode(postalCode: String): Province? {
        val cp = postalCode.trim()
        if (cp.length < 2) return null
        val prefix = cp.substring(0, 2)
        return list.find { it.id == prefix }
    }

    fun findProvinceByName(query: String): Province? {
        val q = query.trim().lowercase()
        return list.find { 
            it.name.lowercase() == q || 
            it.name.lowercase().contains(q) || 
            q.contains(it.name.lowercase())
        }
    }

    fun findNearestProvinceAndIsland(lat: Double, lng: Double): Pair<Province, SpanishIsland?> {
        // 1. Check if coordinates fall in Canary Islands box (~27.0 to 29.8 lat, -18.5 to -13.0 lng)
        if (lat in 26.5..30.0 && lng in -19.0..-13.0) {
            val canaryIslands = islands.filter { it.ccaa == "Canarias" }
            val nearestCanaryIsland = canaryIslands.minByOrNull {
                distanceSquared(lat, lng, it.defaultLat, it.defaultLng)
            }
            if (nearestCanaryIsland != null) {
                val prov = findById(nearestCanaryIsland.provinceId)
                return Pair(prov, nearestCanaryIsland)
            }
        }

        // 2. Check if coordinates fall in Balearic Islands box (~38.4 to 40.4 lat, 1.0 to 4.6 lng)
        if (lat in 38.0..40.8 && lng in 1.0..4.8) {
            val balearicIslands = islands.filter { it.ccaa == "Illes Balears" }
            val nearestBalearicIsland = balearicIslands.minByOrNull {
                distanceSquared(lat, lng, it.defaultLat, it.defaultLng)
            }
            val balearicProvince = findById("07")
            return Pair(balearicProvince, nearestBalearicIsland)
        }

        // 3. Mainland Spain / Ceuta / Melilla: Find nearest province centroid
        val nearestProvince = list.minByOrNull {
            distanceSquared(lat, lng, it.defaultLat, it.defaultLng)
        } ?: defaultProvince

        return Pair(nearestProvince, null)
    }

    private fun distanceSquared(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = lat1 - lat2
        val dLng = lng1 - lng2
        return (dLat * dLat) + (dLng * dLng)
    }
}
