package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_expenses")
data class FuelExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val personId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val stationName: String,
    val stationAddress: String,
    val fuelType: String,
    val liters: Double,
    val pricePerLiter: Double,
    val totalCostEuros: Double,
    val odometerKm: Double,
    val isFullTank: Boolean = true,
    val notes: String = ""
)
