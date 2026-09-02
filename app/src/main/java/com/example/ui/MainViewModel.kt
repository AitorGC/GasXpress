package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.FavoriteStationEntity
import com.example.data.local.entity.FuelExpenseEntity
import com.example.data.local.entity.PersonEntity
import com.example.data.local.entity.VehicleEntity
import com.example.data.model.FuelType
import com.example.data.model.GasStation
import com.example.data.model.Province
import com.example.data.model.SpanishIsland
import com.example.data.model.SpanishProvinces
import com.example.data.repository.AppThemeMode
import com.example.data.repository.ExpenseRepository
import com.example.data.repository.GasStationRepository
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.VehicleRepository
import com.example.domain.notification.PriceNotificationManager
import com.example.domain.pdf.PdfReportGenerator
import com.example.util.LocationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

enum class TripPriceMode {
    ZONE_AVERAGE,
    FAVORITE_STATION,
    CHEAPEST_STATION,
    CUSTOM
}

data class TripCalculationResult(
    val totalDistanceKm: Double,
    val litersNeeded: Double,
    val fuelCost: Double,
    val tollsCost: Double,
    val totalCost: Double,
    val costPerPassenger: Double,
    val costPerKm: Double,
    val priceUsedPerLiter: Double,
    val priceSourceName: String
)

data class StationsUiState(
    val isLoading: Boolean = false,
    val stations: List<GasStation> = emptyList(),
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val filterBrand: String? = null,
    val filterIsland: String? = null,
    val availableIslands: List<SpanishIsland> = emptyList(),
    val filterOnly24h: Boolean = false,
    val filterOnlyFavorites: Boolean = false,
    val sortBy: StationSortOption = StationSortOption.CHEAPEST,
    val minPrice: Double? = null,
    val avgPrice: Double? = null,
    val maxPrice: Double? = null
)

enum class StationSortOption {
    CHEAPEST,
    DISTANCE,
    NAME
}

class MainViewModel(
    private val gasStationRepo: GasStationRepository,
    private val vehicleRepo: VehicleRepository,
    private val expenseRepo: ExpenseRepository,
    private val prefsRepo: PreferencesRepository,
    private val notificationManager: PriceNotificationManager
) : ViewModel() {

    // User Preferences
    val userSettings = prefsRepo.settings

    // Local Data Flows
    val vehicles: StateFlow<List<VehicleEntity>> = vehicleRepo.allVehicles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val persons: StateFlow<List<PersonEntity>> = vehicleRepo.allPersons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<FuelExpenseEntity>> = expenseRepo.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<FavoriteStationEntity>> = gasStationRepo.favoriteStations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Stations State
    private val _stationsState = MutableStateFlow(StationsUiState())
    val stationsState: StateFlow<StationsUiState> = _stationsState.asStateFlow()

    // Notification / price drop snackbar message
    private val _notificationMessage = MutableSharedFlow<String>()
    val notificationMessage = _notificationMessage.asSharedFlow()

    // Trip Calculator State
    private val _tripOrigin = MutableStateFlow("Madrid")
    val tripOrigin: StateFlow<String> = _tripOrigin.asStateFlow()

    private val _tripDestination = MutableStateFlow("Valencia")
    val tripDestination: StateFlow<String> = _tripDestination.asStateFlow()

    private val _tripDistance = MutableStateFlow("355")
    val tripDistance: StateFlow<String> = _tripDistance.asStateFlow()

    private val _tripPriceMode = MutableStateFlow(TripPriceMode.ZONE_AVERAGE)
    val tripPriceMode: StateFlow<TripPriceMode> = _tripPriceMode.asStateFlow()

    private val _selectedTripVehicle = MutableStateFlow<VehicleEntity?>(null)
    val selectedTripVehicle: StateFlow<VehicleEntity?> = _selectedTripVehicle.asStateFlow()

    private val _customConsumption = MutableStateFlow("6.0")
    val customConsumption: StateFlow<String> = _customConsumption.asStateFlow()

    private val _customFuelPrice = MutableStateFlow("1.50")
    val customFuelPrice: StateFlow<String> = _customFuelPrice.asStateFlow()

    private val _isRoundTrip = MutableStateFlow(false)
    val isRoundTrip: StateFlow<Boolean> = _isRoundTrip.asStateFlow()

    private val _passengersCount = MutableStateFlow(1)
    val passengersCount: StateFlow<Int> = _passengersCount.asStateFlow()

    private val _tollsCost = MutableStateFlow("0.0")
    val tollsCost: StateFlow<String> = _tollsCost.asStateFlow()

    // Selected Station for Detail Dialog
    private val _selectedStationForDetail = MutableStateFlow<GasStation?>(null)
    val selectedStationForDetail: StateFlow<GasStation?> = _selectedStationForDetail.asStateFlow()

    // GPS location detection status
    private val _isDetectingLocation = MutableStateFlow(false)
    val isDetectingLocation: StateFlow<Boolean> = _isDetectingLocation.asStateFlow()

    // Generated PDF
    private val _latestGeneratedPdf = MutableStateFlow<File?>(null)
    val latestGeneratedPdf: StateFlow<File?> = _latestGeneratedPdf.asStateFlow()

    private var rawStationsList: List<GasStation> = emptyList()

    init {
        // Initial load
        viewModelScope.launch {
            userSettings.collectLatest { settings ->
                loadStations(
                    provinceId = settings.selectedProvinceId,
                    islandId = settings.selectedIslandId,
                    fuelType = settings.selectedFuelType
                )
            }
        }
    }

    fun loadStations(
        provinceId: String? = null,
        islandId: String? = null,
        fuelType: FuelType? = null,
        forceRefresh: Boolean = false
    ) {
        val targetProvinceId = provinceId ?: userSettings.value.selectedProvinceId
        val targetIslandId = islandId ?: userSettings.value.selectedIslandId
        val targetFuelType = fuelType ?: userSettings.value.selectedFuelType
        val islands = SpanishProvinces.getIslandsForProvince(targetProvinceId)

        viewModelScope.launch {
            _stationsState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    availableIslands = islands,
                    filterIsland = targetIslandId
                )
            }
            val result = gasStationRepo.getStationsForProvince(targetProvinceId, targetIslandId, forceRefresh)
            result.onSuccess { stations ->
                rawStationsList = stations
                applyStationFiltersAndSort(targetFuelType)
                // Check price drops for favorites in background
                val drops = notificationManager.checkPriceDrops(stations)
                if (drops > 0) {
                    _notificationMessage.emit("¡Se han detectado bajadas de precio en $drops estaciones favoritas!")
                }
            }.onFailure { error ->
                _stationsState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Error al cargar estaciones") }
            }
        }
    }

    fun setProvince(provinceId: String, islandId: String? = null) {
        prefsRepo.setProvince(provinceId, islandId)
        val islands = SpanishProvinces.getIslandsForProvince(provinceId)
        _stationsState.update {
            it.copy(
                availableIslands = islands,
                filterIsland = islandId
            )
        }
        loadStations(provinceId = provinceId, islandId = islandId, forceRefresh = true)
    }

    fun detectLocationByGps(
        context: Context,
        onLocationFound: ((Province, SpanishIsland?) -> Unit)? = null
    ) {
        viewModelScope.launch {
            if (!LocationHelper.hasLocationPermission(context)) {
                _notificationMessage.emit("Por favor, concede permiso de ubicación para detectar tu provincia.")
                return@launch
            }

            _isDetectingLocation.value = true
            try {
                val loc = LocationHelper.requestSingleLocationUpdate(context)
                if (loc != null) {
                    val (detectedProvince, detectedIsland) = LocationHelper.resolveProvinceAndIsland(context, loc)
                    setProvince(detectedProvince.id, detectedIsland?.id)
                    val zoneName = if (detectedIsland != null) "${detectedIsland.name} (${detectedProvince.name})" else detectedProvince.name
                    _notificationMessage.emit("📍 Ubicación detectada: $zoneName")
                    onLocationFound?.invoke(detectedProvince, detectedIsland)
                } else {
                    _notificationMessage.emit("No se pudo obtener la posición GPS. Selecciona tu provincia manualmente.")
                }
            } catch (e: Exception) {
                _notificationMessage.emit("Error al obtener ubicación: ${e.message}")
            } finally {
                _isDetectingLocation.value = false
            }
        }
    }

    fun selectIslandFilter(islandId: String?) {
        prefsRepo.setIsland(islandId)
        _stationsState.update { it.copy(filterIsland = islandId) }
        applyStationFiltersAndSort(userSettings.value.selectedFuelType)
    }

    fun setFuelType(fuelType: FuelType) {
        prefsRepo.setFuelType(fuelType)
        applyStationFiltersAndSort(fuelType)
    }

    fun completeOnboarding() {
        prefsRepo.completeOnboarding()
    }

    fun setSearchQuery(query: String) {
        _stationsState.update { it.copy(searchQuery = query) }
        applyStationFiltersAndSort(userSettings.value.selectedFuelType)
    }

    fun setBrandFilter(brand: String?) {
        _stationsState.update { it.copy(filterBrand = brand) }
        applyStationFiltersAndSort(userSettings.value.selectedFuelType)
    }

    fun toggle24hFilter() {
        _stationsState.update { it.copy(filterOnly24h = !it.filterOnly24h) }
        applyStationFiltersAndSort(userSettings.value.selectedFuelType)
    }

    fun setSortOption(option: StationSortOption) {
        _stationsState.update { it.copy(sortBy = option) }
        applyStationFiltersAndSort(userSettings.value.selectedFuelType)
    }

    fun selectStationForDetail(station: GasStation?) {
        _selectedStationForDetail.value = station
    }

    fun toggleFavorite(station: GasStation) {
        viewModelScope.launch {
            gasStationRepo.toggleFavorite(station, userSettings.value.selectedFuelType)
            // Update raw list with new fav status
            rawStationsList = rawStationsList.map {
                if (it.id == station.id) it.copy(isFavorite = !it.isFavorite) else it
            }
            applyStationFiltersAndSort(userSettings.value.selectedFuelType)
        }
    }

    fun simulatePriceDropCheck() {
        viewModelScope.launch {
            if (rawStationsList.isEmpty()) {
                _notificationMessage.emit("Cargando estaciones...")
                return@launch
            }
            val favs = favorites.value
            if (favs.isEmpty()) {
                _notificationMessage.emit("Marca alguna gasolinera como favorita (❤️) para recibir alertas.")
                return@launch
            }
            // Trigger test notification for the first favorite
            val firstFav = favs.first()
            val fuel = FuelType.fromId(firstFav.preferredFuel)
            val currentPrice = firstFav.lastKnownPrice.takeIf { it > 0 } ?: fuel.defaultAvgPrice
            val newPrice = (currentPrice - 0.035).coerceAtLeast(0.8)
            notificationManager.sendPriceDropNotification(
                stationName = firstFav.name,
                fuelName = fuel.displayName,
                newPrice = newPrice,
                oldPrice = currentPrice,
                savings = 0.035
            )
            _notificationMessage.emit("¡Notificación enviada! Precio rebajado en ${firstFav.name}")
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefsRepo.setThemeMode(mode)
    }

    fun toggleFavoriteFilter() {
        _stationsState.update { it.copy(filterOnlyFavorites = !it.filterOnlyFavorites) }
        applyStationFiltersAndSort(userSettings.value.selectedFuelType)
    }

    private fun applyStationFiltersAndSort(fuelType: FuelType) {
        val query = _stationsState.value.searchQuery.trim().lowercase()
        val brand = _stationsState.value.filterBrand
        val activeIsland = _stationsState.value.filterIsland
        val only24h = _stationsState.value.filterOnly24h
        val onlyFavs = _stationsState.value.filterOnlyFavorites
        val favIds = favorites.value.map { it.stationId }.toSet()
        val sort = _stationsState.value.sortBy

        var filtered = rawStationsList.filter { station ->
            val hasFuel = station.getPriceFor(fuelType) != null
            val matchIsland = activeIsland.isNullOrBlank() || station.islandId.equals(activeIsland, ignoreCase = true)
            val matchQuery = query.isEmpty() ||
                    station.name.lowercase().contains(query) ||
                    station.address.lowercase().contains(query) ||
                    station.municipality.lowercase().contains(query) ||
                    station.postalCode.contains(query) ||
                    (station.islandName?.lowercase()?.contains(query) == true)
            val matchBrand = brand == null || station.brandCategory.name.equals(brand, ignoreCase = true)
            val match24h = !only24h || station.is24h
            val matchFav = !onlyFavs || favIds.contains(station.id) || station.isFavorite

            hasFuel && matchIsland && matchQuery && matchBrand && match24h && matchFav
        }

        // Calculate statistics
        val prices = filtered.mapNotNull { it.getPriceFor(fuelType) }
        val min = prices.minOrNull()
        val max = prices.maxOrNull()
        val avg = if (prices.isNotEmpty()) prices.average() else null

        // Sort
        filtered = when (sort) {
            StationSortOption.CHEAPEST -> filtered.sortedBy { it.getPriceFor(fuelType) ?: Double.MAX_VALUE }
            StationSortOption.DISTANCE -> filtered.sortedBy { it.distanceKm ?: Double.MAX_VALUE }
            StationSortOption.NAME -> filtered.sortedBy { it.name }
        }

        _stationsState.update {
            it.copy(
                isLoading = false,
                stations = filtered,
                minPrice = min,
                avgPrice = avg,
                maxPrice = max
            )
        }
    }

    // Trip Calculator
    fun setTripOrigin(origin: String) { _tripOrigin.value = origin }
    fun setTripDestination(destination: String) { _tripDestination.value = destination }
    fun setTripDistance(dist: String) { _tripDistance.value = dist }
    fun setTripPreset(origin: String, destination: String, distKm: String) {
        _tripOrigin.value = origin
        _tripDestination.value = destination
        _tripDistance.value = distKm
    }
    fun swapTripOriginDestination() {
        val temp = _tripOrigin.value
        _tripOrigin.value = _tripDestination.value
        _tripDestination.value = temp
    }
    fun setTripPriceMode(mode: TripPriceMode) { _tripPriceMode.value = mode }
    fun setCustomConsumption(cons: String) { _customConsumption.value = cons }
    fun setCustomFuelPrice(price: String) { _customFuelPrice.value = price }
    fun setIsRoundTrip(round: Boolean) { _isRoundTrip.value = round }
    fun setPassengersCount(count: Int) { _passengersCount.value = count.coerceAtLeast(1) }
    fun setTollsCost(cost: String) { _tollsCost.value = cost }

    fun selectTripVehicle(vehicle: VehicleEntity?) {
        _selectedTripVehicle.value = vehicle
        if (vehicle != null) {
            _customConsumption.value = String.format(java.util.Locale.US, "%.1f", vehicle.avgConsumptionL100km)
            val fuel = FuelType.fromId(vehicle.fuelType)
            val avgPrice = _stationsState.value.avgPrice ?: fuel.defaultAvgPrice
            _customFuelPrice.value = String.format(java.util.Locale.US, "%.3f", avgPrice)
        }
    }

    fun calculateTrip(): TripCalculationResult {
        val dist = _tripDistance.value.toDoubleOrNull() ?: 0.0
        val actualDistance = if (_isRoundTrip.value) dist * 2.0 else dist
        val consumption = _customConsumption.value.toDoubleOrNull() ?: 6.0
        val tolls = _tollsCost.value.toDoubleOrNull() ?: 0.0
        val pass = _passengersCount.value.coerceAtLeast(1)

        val fuelType = _selectedTripVehicle.value?.let { FuelType.fromId(it.fuelType) } ?: userSettings.value.selectedFuelType
        val avgZonePrice = _stationsState.value.avgPrice ?: fuelType.defaultAvgPrice
        val minZonePrice = _stationsState.value.minPrice ?: avgZonePrice

        // Find favorite station price
        val favIds = favorites.value.map { it.stationId }.toSet()
        val favStationPrice = rawStationsList.filter { favIds.contains(it.id) }
            .mapNotNull { it.getPriceFor(fuelType) }
            .minOrNull()
            ?: favorites.value.firstOrNull()?.lastKnownPrice?.takeIf { it > 0 }
            ?: avgZonePrice

        val (resolvedPrice, priceSourceName) = when (_tripPriceMode.value) {
            TripPriceMode.ZONE_AVERAGE -> Pair(avgZonePrice, "Precio medio de la zona")
            TripPriceMode.FAVORITE_STATION -> Pair(favStationPrice, "Mi gasolinera favorita")
            TripPriceMode.CHEAPEST_STATION -> Pair(minZonePrice, "Gasolinera más barata")
            TripPriceMode.CUSTOM -> Pair(_customFuelPrice.value.toDoubleOrNull() ?: avgZonePrice, "Precio personalizado")
        }

        val litersNeeded = (actualDistance * consumption) / 100.0
        val fuelCost = litersNeeded * resolvedPrice
        val totalCost = fuelCost + tolls
        val costPerPass = totalCost / pass
        val costPerKm = if (actualDistance > 0) totalCost / actualDistance else 0.0

        return TripCalculationResult(
            totalDistanceKm = actualDistance,
            litersNeeded = litersNeeded,
            fuelCost = fuelCost,
            tollsCost = tolls,
            totalCost = totalCost,
            costPerPassenger = costPerPass,
            costPerKm = costPerKm,
            priceUsedPerLiter = resolvedPrice,
            priceSourceName = priceSourceName
        )
    }

    // Vehicle Management
    fun addVehicle(vehicle: VehicleEntity) {
        viewModelScope.launch { vehicleRepo.insertVehicle(vehicle) }
    }

    fun updateVehicle(vehicle: VehicleEntity) {
        viewModelScope.launch { vehicleRepo.updateVehicle(vehicle) }
    }

    fun deleteVehicle(vehicle: VehicleEntity) {
        viewModelScope.launch { vehicleRepo.deleteVehicle(vehicle) }
    }

    // Person / Driver Management
    fun addPerson(person: PersonEntity) {
        viewModelScope.launch { vehicleRepo.insertPerson(person) }
    }

    fun updatePerson(person: PersonEntity) {
        viewModelScope.launch { vehicleRepo.updatePerson(person) }
    }

    fun deletePerson(person: PersonEntity) {
        viewModelScope.launch { vehicleRepo.deletePerson(person) }
    }

    // Expense Management
    fun addExpense(expense: FuelExpenseEntity) {
        viewModelScope.launch {
            expenseRepo.insertExpense(expense)
            val veh = vehicleRepo.getVehicleById(expense.vehicleId)
            if (veh != null && expense.odometerKm > veh.initialOdometerKm) {
                vehicleRepo.updateVehicle(veh.copy(initialOdometerKm = expense.odometerKm))
            }
        }
    }

    fun updateExpense(expense: FuelExpenseEntity) {
        viewModelScope.launch { expenseRepo.updateExpense(expense) }
    }

    fun deleteExpense(expense: FuelExpenseEntity) {
        viewModelScope.launch { expenseRepo.deleteExpense(expense) }
    }

    // PDF Report Generator
    fun generateMonthlyPdfReport(
        context: Context,
        monthYearText: String,
        targetVehicle: VehicleEntity? = null,
        targetPerson: PersonEntity? = null
    ): File? {
        val currentExpenses = expenses.value
        val currentVehicles = vehicles.value
        val currentPersons = persons.value

        val filter = PdfReportGenerator.ReportFilter(
            monthYearText = monthYearText,
            selectedVehicle = targetVehicle,
            selectedPerson = targetPerson
        )

        val file = PdfReportGenerator.generateMonthlyReport(
            context = context,
            expenses = currentExpenses,
            vehicles = currentVehicles,
            persons = currentPersons,
            filter = filter
        )

        _latestGeneratedPdf.value = file
        return file
    }
}

class MainViewModelFactory(private val app: com.example.GasolinaApp) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(
            gasStationRepo = app.gasStationRepository,
            vehicleRepo = app.vehicleRepository,
            expenseRepo = app.expenseRepository,
            prefsRepo = app.preferencesRepository,
            notificationManager = app.notificationManager
        ) as T
    }
}
