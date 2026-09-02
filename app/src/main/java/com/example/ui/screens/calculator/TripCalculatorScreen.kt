package com.example.ui.screens.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.VehicleEntity
import com.example.data.model.FuelType
import com.example.ui.MainViewModel
import com.example.ui.TripPriceMode
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripCalculatorScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    val tripOrigin by viewModel.tripOrigin.collectAsStateWithLifecycle()
    val tripDestination by viewModel.tripDestination.collectAsStateWithLifecycle()
    val tripDistance by viewModel.tripDistance.collectAsStateWithLifecycle()
    val tripPriceMode by viewModel.tripPriceMode.collectAsStateWithLifecycle()
    val selectedVehicle by viewModel.selectedTripVehicle.collectAsStateWithLifecycle()
    val customConsumption by viewModel.customConsumption.collectAsStateWithLifecycle()
    val customFuelPrice by viewModel.customFuelPrice.collectAsStateWithLifecycle()
    val isRoundTrip by viewModel.isRoundTrip.collectAsStateWithLifecycle()
    val passengersCount by viewModel.passengersCount.collectAsStateWithLifecycle()
    val tollsCost by viewModel.tollsCost.collectAsStateWithLifecycle()
    val stationsState by viewModel.stationsState.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val result by viewModel.tripCalculationResult.collectAsStateWithLifecycle()

    val routePresets = remember(userSettings.selectedProvinceId, userSettings.selectedIslandId) {
        when (userSettings.selectedProvinceId) {
            "38" -> listOf(
                Triple("Santa Cruz", "Puerto de la Cruz", "38"),
                Triple("Santa Cruz", "Los Cristianos", "76"),
                Triple("La Laguna", "Icod de los Vinos", "48"),
                Triple("Santa Cruz", "Adeje", "79"),
                Triple("Santa Cruz La Palma", "Los Llanos", "35"),
                Triple("San Sebastián Gomera", "Valle Gran Rey", "49")
            )
            "35" -> listOf(
                Triple("Las Palmas", "Maspalomas", "55"),
                Triple("Las Palmas", "Telde", "16"),
                Triple("Las Palmas", "Gáldar", "27"),
                Triple("Arrecife", "Playa Blanca", "37"),
                Triple("Puerto del Rosario", "Morro Jable", "85"),
                Triple("Puerto del Rosario", "Corralejo", "31")
            )
            "07" -> listOf(
                Triple("Palma", "Alcúdia", "54"),
                Triple("Palma", "Manacor", "50"),
                Triple("Palma", "Sóller", "26"),
                Triple("Palma", "Andratx", "29"),
                Triple("Maó", "Ciutadella", "45"),
                Triple("Eivissa", "Sant Antoni", "15")
            )
            else -> listOf(
                Triple("Madrid", "Valencia", "355"),
                Triple("Madrid", "Barcelona", "620"),
                Triple("Madrid", "Sevilla", "530"),
                Triple("Barcelona", "Valencia", "350"),
                Triple("Bilbao", "Madrid", "400"),
                Triple("Málaga", "Sevilla", "205"),
                Triple("A Coruña", "Madrid", "590")
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Calculadora de Viaje",
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                windowInsets = WindowInsets(0.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Bento Hero Result Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.08f),
                        modifier = Modifier
                            .size(130.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 20.dp, y = 20.dp)
                    )

                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "COSTE ESTIMADO DEL VIAJE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = String.format(Locale.getDefault(), "%.2f €", result.totalCost),
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Text(
                            text = "${result.priceSourceName} (${String.format(Locale.getDefault(), "%.3f €/L", result.priceUsedPerLiter)})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )

                        if (passengersCount > 1) {
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text(
                                    text = "${String.format(Locale.getDefault(), "%.2f €", result.costPerPassenger)} por persona ($passengersCount viajeros)",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sub-metrics Bento Tiles inside Hero
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("DISTANCIA", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = String.format(Locale.getDefault(), "%.0f km", result.totalDistanceKm),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("LITROS", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = String.format(Locale.getDefault(), "%.1f L", result.litersNeeded),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("COSTE/KM", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = String.format(Locale.getDefault(), "%.3f €", result.costPerKm),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Origin & Destination Bento Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. ORIGEN Y DESTINO",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = tripOrigin,
                                onValueChange = { viewModel.setTripOrigin(it) },
                                label = { Text("Punto de Partida (Origen)") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.TripOrigin, contentDescription = null, tint = Color(0xFF10B981))
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = tripDestination,
                                onValueChange = { viewModel.setTripDestination(it) },
                                label = { Text("Destino") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Place, contentDescription = null, tint = Color(0xFFEF4444))
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        IconButton(
                            onClick = { viewModel.swapTripOriginDestination() },
                            modifier = Modifier
                                .size(42.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Intercambiar origen y destino",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Distance field
                    OutlinedTextField(
                        value = tripDistance,
                        onValueChange = { viewModel.setTripDistance(it) },
                        label = { Text("Distancia en Kilómetros (km)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Route, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = { Text("km  ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Rutas populares directas:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(routePresets) { (orig, dest, dist) ->
                            SuggestionChip(
                                onClick = { viewModel.setTripPreset(orig, dest, dist) },
                                label = { Text("$orig ➔ $dest ($dist km)", fontSize = 11.sp) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Round trip toggle
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Viaje de Ida y Vuelta", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Calcula automáticamente el trayecto x2", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = isRoundTrip,
                                onCheckedChange = { viewModel.setIsRoundTrip(it) }
                            )
                        }
                    }
                }
            }

            // 3. Select Vehicle Bento Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. VEHÍCULO SELECCIONADO",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (vehicles.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Añade tus vehículos en la pestaña 'Vehículos' para seleccionar consumos exactos automáticamente.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(vehicles) { vehicle ->
                                val isSelected = selectedVehicle?.id == vehicle.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (isSelected) viewModel.selectTripVehicle(null)
                                        else viewModel.selectTripVehicle(vehicle)
                                    },
                                    label = {
                                        Text(
                                            text = "${vehicle.name} (${vehicle.avgConsumptionL100km} L)",
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsCar,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Fuel Price Source & Consumption Bento Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. FUENTE DE PRECIOS Y CONSUMO",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Calcular coste según:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = tripPriceMode == TripPriceMode.ZONE_AVERAGE,
                                onClick = { viewModel.setTripPriceMode(TripPriceMode.ZONE_AVERAGE) },
                                label = { Text("📊 Media de la zona", fontSize = 11.sp) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                        item {
                            FilterChip(
                                selected = tripPriceMode == TripPriceMode.FAVORITE_STATION,
                                onClick = { viewModel.setTripPriceMode(TripPriceMode.FAVORITE_STATION) },
                                label = { Text("❤️ Favoritas", fontSize = 11.sp) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                        item {
                            FilterChip(
                                selected = tripPriceMode == TripPriceMode.CHEAPEST_STATION,
                                onClick = { viewModel.setTripPriceMode(TripPriceMode.CHEAPEST_STATION) },
                                label = { Text("⚡ Más Barata", fontSize = 11.sp) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                        item {
                            FilterChip(
                                selected = tripPriceMode == TripPriceMode.CUSTOM,
                                onClick = { viewModel.setTripPriceMode(TripPriceMode.CUSTOM) },
                                label = { Text("✏️ Personalizado", fontSize = 11.sp) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = customConsumption,
                            onValueChange = { viewModel.setCustomConsumption(it) },
                            label = { Text("Consumo") },
                            trailingIcon = { Text("L/100km ", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        )

                        if (tripPriceMode == TripPriceMode.CUSTOM) {
                            OutlinedTextField(
                                value = customFuelPrice,
                                onValueChange = { viewModel.setCustomFuelPrice(it) },
                                label = { Text("Precio Gasolina") },
                                trailingIcon = { Text("€/L ", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("Precio aplicado", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = String.format(Locale.getDefault(), "%.3f €/L", result.priceUsedPerLiter),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Passengers & Tolls Bento Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "4. VIAJEROS Y PEAJES",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Passengers counter
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Pasajeros", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    IconButton(
                                        onClick = { viewModel.setPassengersCount(passengersCount - 1) },
                                        enabled = passengersCount > 1,
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Menos")
                                    }
                                    Text(
                                        text = "$passengersCount",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp
                                    )
                                    IconButton(
                                        onClick = { viewModel.setPassengersCount(passengersCount + 1) },
                                        enabled = passengersCount < 10,
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "Más")
                                    }
                                }
                            }
                        }

                        // Tolls
                        OutlinedTextField(
                            value = tollsCost,
                            onValueChange = { viewModel.setTollsCost(it) },
                            label = { Text("Peajes / Extras") },
                            trailingIcon = { Text("€ ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
