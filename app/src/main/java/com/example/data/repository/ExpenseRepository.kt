package com.example.data.repository

import com.example.data.local.dao.FuelExpenseDao
import com.example.data.local.entity.FuelExpenseEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ExpenseRepository(
    private val expenseDao: FuelExpenseDao
) {
    val allExpenses: Flow<List<FuelExpenseEntity>> = expenseDao.getAllExpenses()

    fun getExpensesByVehicle(vehicleId: Long): Flow<List<FuelExpenseEntity>> =
        expenseDao.getExpensesByVehicle(vehicleId)

    fun getExpensesByPerson(personId: Long): Flow<List<FuelExpenseEntity>> =
        expenseDao.getExpensesByPerson(personId)

    suspend fun getAllExpensesList(): List<FuelExpenseEntity> = withContext(Dispatchers.IO) {
        expenseDao.getAllExpensesList()
    }

    suspend fun insertExpense(expense: FuelExpenseEntity): Long = withContext(Dispatchers.IO) {
        expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: FuelExpenseEntity) = withContext(Dispatchers.IO) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: FuelExpenseEntity) = withContext(Dispatchers.IO) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun deleteExpenseById(id: Long) = withContext(Dispatchers.IO) {
        expenseDao.deleteExpenseById(id)
    }
}
