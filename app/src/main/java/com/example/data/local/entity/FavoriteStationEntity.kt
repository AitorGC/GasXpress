package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_stations")
data class FavoriteStationEntity(
    @PrimaryKey val stationId: String,
    val name: String,
    val address: String,
    val municipality: String,
    val province: String,
    val preferredFuel: String = "gasolina95",
    val lastKnownPrice: Double = 0.0,
    val lastNotifiedPrice: Double = 0.0,
    val addedTimestamp: Long = System.currentTimeMillis()
)
