package com.example.data.repository

import com.example.data.local.dao.PersonDao
import com.example.data.local.dao.VehicleDao
import com.example.data.local.entity.PersonEntity
import com.example.data.local.entity.VehicleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class VehicleRepository(
    private val vehicleDao: VehicleDao,
    private val personDao: PersonDao
) {
    val allVehicles: Flow<List<VehicleEntity>> = vehicleDao.getAllVehicles()
    val allPersons: Flow<List<PersonEntity>> = personDao.getAllPersons()

    suspend fun getVehicleById(id: Long): VehicleEntity? = withContext(Dispatchers.IO) {
        vehicleDao.getVehicleById(id)
    }

    suspend fun insertVehicle(vehicle: VehicleEntity): Long = withContext(Dispatchers.IO) {
        vehicleDao.insertVehicle(vehicle)
    }

    suspend fun updateVehicle(vehicle: VehicleEntity) = withContext(Dispatchers.IO) {
        vehicleDao.updateVehicle(vehicle)
    }

    suspend fun deleteVehicle(vehicle: VehicleEntity) = withContext(Dispatchers.IO) {
        vehicleDao.deleteVehicle(vehicle)
    }

    suspend fun insertPerson(person: PersonEntity): Long = withContext(Dispatchers.IO) {
        personDao.insertPerson(person)
    }

    suspend fun updatePerson(person: PersonEntity) = withContext(Dispatchers.IO) {
        personDao.updatePerson(person)
    }

    suspend fun deletePerson(person: PersonEntity) = withContext(Dispatchers.IO) {
        personDao.deletePerson(person)
    }
}
