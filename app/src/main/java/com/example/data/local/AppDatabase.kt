package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.FavoriteStationDao
import com.example.data.local.dao.FuelExpenseDao
import com.example.data.local.dao.PersonDao
import com.example.data.local.dao.VehicleDao
import com.example.data.local.entity.FavoriteStationEntity
import com.example.data.local.entity.FuelExpenseEntity
import com.example.data.local.entity.PersonEntity
import com.example.data.local.entity.VehicleEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PersonEntity::class,
        VehicleEntity::class,
        FuelExpenseEntity::class,
        FavoriteStationEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun vehicleDao(): VehicleDao
    abstract fun fuelExpenseDao(): FuelExpenseDao
    abstract fun favoriteStationDao(): FavoriteStationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gasolina_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(db: AppDatabase) {
            val personDao = db.personDao()
            val vehicleDao = db.vehicleDao()
            val expenseDao = db.fuelExpenseDao()

            if (personDao.getCount() == 0) {
                val papaId = personDao.insertPerson(
                    PersonEntity(
                        name = "Papá",
                        relationship = "Padre",
                        avatarEmoji = "👨",
                        avatarColorHex = 0xFF0284C7
                    )
                )
                val mamaId = personDao.insertPerson(
                    PersonEntity(
                        name = "Mamá",
                        relationship = "Madre",
                        avatarEmoji = "👩",
                        avatarColorHex = 0xFFEC4899
                    )
                )
                val hijoId = personDao.insertPerson(
                    PersonEntity(
                        name = "Hijo",
                        relationship = "Hijo",
                        avatarEmoji = "👦",
                        avatarColorHex = 0xFF10B981
                    )
                )

                if (vehicleDao.getCount() == 0) {
                    val v1Id = vehicleDao.insertVehicle(
                        VehicleEntity(
                            name = "Seat León 1.5 TSI",
                            brand = "SEAT",
                            model = "León 1.5 TSI",
                            licensePlate = "4812 LXZ",
                            fuelType = "gasolina95",
                            tankCapacityLiters = 50.0,
                            avgConsumptionL100km = 6.1,
                            assignedPersonId = papaId,
                            colorHex = 0xFF0284C7,
                            initialOdometerKm = 45200.0
                        )
                    )
                    val v2Id = vehicleDao.insertVehicle(
                        VehicleEntity(
                            name = "Toyota Yaris Hybrid",
                            brand = "Toyota",
                            model = "Yaris Hybrid",
                            licensePlate = "9123 MNB",
                            fuelType = "gasolina95",
                            tankCapacityLiters = 36.0,
                            avgConsumptionL100km = 4.3,
                            assignedPersonId = mamaId,
                            colorHex = 0xFFEC4899,
                            initialOdometerKm = 21400.0
                        )
                    )

                    // Seed a couple of realistic recent expenses
                    val now = System.currentTimeMillis()
                    val dayMs = 24L * 60 * 60 * 1000
                    expenseDao.insertExpense(
                        FuelExpenseEntity(
                            vehicleId = v1Id,
                            personId = papaId,
                            timestamp = now - (5 * dayMs),
                            stationName = "Plenoil",
                            stationAddress = "Av. de Andalucía, 32",
                            fuelType = "gasolina95",
                            liters = 42.50,
                            pricePerLiter = 1.489,
                            totalCostEuros = 63.28,
                            odometerKm = 45850.0,
                            isFullTank = true,
                            notes = "Llenado completo antes de viaje de fin de semana"
                        )
                    )
                    expenseDao.insertExpense(
                        FuelExpenseEntity(
                            vehicleId = v2Id,
                            personId = mamaId,
                            timestamp = now - (2 * dayMs),
                            stationName = "Repsol",
                            stationAddress = "Calle Alcalá, 240",
                            fuelType = "gasolina95",
                            liters = 28.00,
                            pricePerLiter = 1.579,
                            totalCostEuros = 44.21,
                            odometerKm = 21980.0,
                            isFullTank = true,
                            notes = "Trayecto urbano diario"
                        )
                    )
                }
            }
        }
    }
}
