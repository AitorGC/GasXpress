package com.example.data.model

enum class FuelType(
    val id: String,
    val displayName: String,
    val shortName: String,
    val defaultAvgPrice: Double
) {
    GASOLINA_95("gasolina95", "Gasolina 95 E5", "G95", 1.549),
    DIESEL_A("dieselA", "Gasóleo A (Diésel)", "Diésel", 1.449),
    GASOLINA_98("gasolina98", "Gasolina 98 E5", "G98", 1.689),
    DIESEL_PREMIUM("dieselPremium", "Gasóleo Premium", "Diésel+", 1.539),
    GLP("glp", "Autogás / GLP", "GLP", 0.949),
    GNC("gnc", "Gas Natural (GNC)", "GNC", 1.159),
    DIESEL_B("dieselB", "Gasóleo B (Agrícola)", "Diésel B", 1.129);

    companion object {
        fun fromId(id: String?): FuelType {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: GASOLINA_95
        }
    }
}
