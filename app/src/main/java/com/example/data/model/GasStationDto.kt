package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GasStationResponseDto(
    @Json(name = "Fecha") val fecha: String? = null,
    @Json(name = "ListaEESSPrecio") val listaEESSPrecio: List<GasStationDto>? = null,
    @Json(name = "Nota") val nota: String? = null,
    @Json(name = "ResultadoConsulta") val resultadoConsulta: String? = null
)

@JsonClass(generateAdapter = true)
data class GasStationDto(
    @Json(name = "IDEESS") val id: String? = null,
    @Json(name = "Rótulo") val rotulo: String? = null,
    @Json(name = "Dirección") val direccion: String? = null,
    @Json(name = "C.P.") val codigoPostal: String? = null,
    @Json(name = "Localidad") val localidad: String? = null,
    @Json(name = "Municipio") val municipio: String? = null,
    @Json(name = "Provincia") val provincia: String? = null,
    @Json(name = "Horario") val horario: String? = null,
    @Json(name = "Latitud") val latitud: String? = null,
    @Json(name = "Longitud (WGS84)") val longitud: String? = null,
    @Json(name = "IDMunicipio") val idMunicipio: String? = null,
    @Json(name = "IDProvincia") val idProvincia: String? = null,
    @Json(name = "IDCCAA") val idCCAA: String? = null,
    @Json(name = "Tipo Venta") val tipoVenta: String? = null,
    @Json(name = "Margen") val margen: String? = null,
    
    // Prices with commas e.g. "1,459"
    @Json(name = "Precio Gasolina 95 E5") val precioGasolina95E5: String? = null,
    @Json(name = "Precio Gasolina 98 E5") val precioGasolina98E5: String? = null,
    @Json(name = "Precio Gasoleo A") val precioGasoleoA: String? = null,
    @Json(name = "Precio Gasoleo Premium") val precioGasoleoPremium: String? = null,
    @Json(name = "Precio Gasoleo B") val precioGasoleoB: String? = null,
    @Json(name = "Precio Gases licuados del petróleo") val precioGLP: String? = null,
    @Json(name = "Precio Gas Natural Comprimido") val precioGNC: String? = null,
    @Json(name = "Precio Biodiesel") val precioBiodiesel: String? = null,
    @Json(name = "Precio Bioetanol") val precioBioetanol: String? = null,
    @Json(name = "Precio Hidrogeno") val precioHidrogeno: String? = null
)
