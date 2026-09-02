package com.example.data.network

import com.example.data.model.GasStationResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

interface MitecoApiService {

    @GET("EstacionesTerrestres/")
    suspend fun getAllGasStations(): GasStationResponseDto

    @GET("EstacionesTerrestres/FiltroProvincia/{idProvincia}")
    suspend fun getGasStationsByProvince(
        @Path("idProvincia") idProvincia: String
    ): GasStationResponseDto

    @GET("EstacionesTerrestres/FiltroMunicipio/{idMunicipio}")
    suspend fun getGasStationsByMunicipality(
        @Path("idMunicipio") idMunicipio: String
    ): GasStationResponseDto
}
