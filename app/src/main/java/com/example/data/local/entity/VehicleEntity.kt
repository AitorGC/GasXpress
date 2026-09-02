package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "vehicles",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["assignedPersonId"],
            onDelete = androidx.room.ForeignKey.SET_NULL
        )
    ],
    indices = [androidx.room.Index("assignedPersonId")]
)
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // e.g. "Seat León 1.5 TSI"
    val brand: String = "", // e.g. "SEAT"
    val model: String = "", // e.g. "León 1.5 TSI"
    val licensePlate: String, // e.g. "8492 KVM"
    val fuelType: String = "gasolina95", // FuelType id
    val tankCapacityLiters: Double = 50.0,
    val avgConsumptionL100km: Double = 6.2,
    val assignedPersonId: Long? = null,
    val colorHex: Long = 0xFF0284C7,
    val iconName: String = "car",
    val initialOdometerKm: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
