package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.network.ApiClient
import com.example.data.repository.ExpenseRepository
import com.example.data.repository.GasStationRepository
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.VehicleRepository
import com.example.domain.notification.PriceNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class GasolinaApp : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }

    override fun onCreate() {
        super.onCreate()
        ApiClient.initialize(this)
    }

    val gasStationRepository by lazy {
        GasStationRepository(
            apiService = ApiClient.mitecoApi,
            favoriteDao = database.favoriteStationDao()
        )
    }

    val vehicleRepository by lazy {
        VehicleRepository(
            vehicleDao = database.vehicleDao(),
            personDao = database.personDao()
        )
    }

    val expenseRepository by lazy {
        ExpenseRepository(
            expenseDao = database.fuelExpenseDao()
        )
    }

    val preferencesRepository by lazy {
        PreferencesRepository(this)
    }

    val notificationManager by lazy {
        PriceNotificationManager(
            context = this,
            favoriteDao = database.favoriteStationDao()
        )
    }
}
