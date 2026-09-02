package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "persons")
data class PersonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val relationship: String, // e.g. "Papá", "Mamá", "Esposa", "Hijo", "Hija", "Titular"
    val avatarEmoji: String = "👤",
    val avatarColorHex: Long = 0xFF0284C7,
    val createdAt: Long = System.currentTimeMillis()
)
