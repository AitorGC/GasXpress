package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.FuelExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelExpenseDao {
    @Query("SELECT * FROM fuel_expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<FuelExpenseEntity>>

    @Query("SELECT * FROM fuel_expenses WHERE vehicleId = :vehicleId ORDER BY timestamp DESC")
    fun getExpensesByVehicle(vehicleId: Long): Flow<List<FuelExpenseEntity>>

    @Query("SELECT * FROM fuel_expenses WHERE personId = :personId ORDER BY timestamp DESC")
    fun getExpensesByPerson(personId: Long): Flow<List<FuelExpenseEntity>>

    @Query("SELECT * FROM fuel_expenses WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getExpensesBetween(startTime: Long, endTime: Long): Flow<List<FuelExpenseEntity>>

    @Query("SELECT * FROM fuel_expenses ORDER BY timestamp DESC")
    suspend fun getAllExpensesList(): List<FuelExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: FuelExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: FuelExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: FuelExpenseEntity)

    @Query("DELETE FROM fuel_expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)
}
