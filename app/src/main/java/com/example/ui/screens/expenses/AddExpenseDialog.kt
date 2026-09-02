package com.example.ui.screens.expenses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.FuelExpenseEntity
import com.example.data.local.entity.PersonEntity
import com.example.data.local.entity.VehicleEntity
import com.example.data.model.FuelType
import com.example.data.model.GasStation
import java.util.Locale

@Composable
fun AddExpenseDialog(
    prefilledStation: GasStation? = null,
    vehicles: List<VehicleEntity>,
    persons: List<PersonEntity>,
    onSave: (FuelExpenseEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedVehicle by remember { mutableStateOf(vehicles.firstOrNull()) }
    var selectedPerson by remember {
        mutableStateOf(
            selectedVehicle?.assignedPersonId?.let { pId -> persons.find { it.id == pId } } ?: persons.firstOrNull()
        )
    }

    var stationName by remember { mutableStateOf(prefilledStation?.name ?: "Gasolinera") }
    var stationAddress by remember { mutableStateOf(prefilledStation?.address ?: "") }
    var selectedFuelType by remember {
        mutableStateOf(
            selectedVehicle?.let { FuelType.fromId(it.fuelType) } ?: FuelType.GASOLINA_95
        )
    }

    val initialPrice = prefilledStation?.getPriceFor(selectedFuelType) ?: selectedFuelType.defaultAvgPrice
    var pricePerLiterStr by remember { mutableStateOf(String.format(Locale.US, "%.3f", initialPrice)) }
    var litersStr by remember { mutableStateOf("40.0") }
    var totalCostStr by remember { mutableStateOf(String.format(Locale.US, "%.2f", 40.0 * initialPrice)) }
    var odometerStr by remember { mutableStateOf(selectedVehicle?.initialOdometerKm?.toInt()?.toString() ?: "") }
    var isFullTank by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }

    // Sync vehicle fuel
    LaunchedEffect(selectedVehicle) {
        selectedVehicle?.let { v ->
            selectedFuelType = FuelType.fromId(v.fuelType)
            v.assignedPersonId?.let { pId ->
                persons.find { it.id == pId }?.let { selectedPerson = it }
            }
        }
    }

    fun recalculateTotal() {
        val lit = litersStr.toDoubleOrNull() ?: 0.0
        val price = pricePerLiterStr.toDoubleOrNull() ?: 0.0
        if (lit > 0 && price > 0) {
            totalCostStr = String.format(Locale.US, "%.2f", lit * price)
        }
    }

    fun recalculateLiters() {
        val cost = totalCostStr.toDoubleOrNull() ?: 0.0
        val price = pricePerLiterStr.toDoubleOrNull() ?: 0.0
        if (cost > 0 && price > 0) {
            litersStr = String.format(Locale.US, "%.2f", cost / price)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.LocalGasStation, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Registrar Repostaje", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Vehicle Selector
                Text("Vehículo:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                if (vehicles.isEmpty()) {
                    Text("No hay vehículos creados.", fontSize = 12.sp, color = Color.Red)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(vehicles) { vehicle ->
                            FilterChip(
                                selected = selectedVehicle?.id == vehicle.id,
                                onClick = { selectedVehicle = vehicle },
                                label = { Text("${vehicle.name} (${vehicle.licensePlate})", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // 2. Conductor / Person
                Text("Conductor / Persona:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                if (persons.isEmpty()) {
                    Text("No hay conductores creados.", fontSize = 12.sp, color = Color.Gray)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(persons) { person ->
                            FilterChip(
                                selected = selectedPerson?.id == person.id,
                                onClick = { selectedPerson = person },
                                label = { Text("${person.avatarEmoji} ${person.name}", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // 3. Station info
                OutlinedTextField(
                    value = stationName,
                    onValueChange = { stationName = it },
                    label = { Text("Estación de Servicio") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // 4. Fuel Type
                Text("Combustible:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(FuelType.entries) { fuel ->
                        FilterChip(
                            selected = selectedFuelType == fuel,
                            onClick = {
                                selectedFuelType = fuel
                                val p = prefilledStation?.getPriceFor(fuel) ?: fuel.defaultAvgPrice
                                pricePerLiterStr = String.format(Locale.US, "%.3f", p)
                                recalculateTotal()
                            },
                            label = { Text(fuel.shortName, fontSize = 11.sp) }
                        )
                    }
                }

                // 5. Liters, Price, and Total Cost
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = litersStr,
                        onValueChange = {
                            litersStr = it
                            recalculateTotal()
                        },
                        label = { Text("Litros (L)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = pricePerLiterStr,
                        onValueChange = {
                            pricePerLiterStr = it
                            recalculateTotal()
                        },
                        label = { Text("Precio (€/L)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = totalCostStr,
                    onValueChange = {
                        totalCostStr = it
                        recalculateLiters()
                    },
                    label = { Text("Coste Total (€)") },
                    trailingIcon = { Text("€ ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // 6. Odometer & Full Tank
                OutlinedTextField(
                    value = odometerStr,
                    onValueChange = { odometerStr = it },
                    label = { Text("Kilometraje (Odómetro)") },
                    trailingIcon = { Text("km ", fontSize = 11.sp, color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("¿Depósito Lleno?", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Switch(checked = isFullTank, onCheckedChange = { isFullTank = it })
                }

                // 7. Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas (opcional)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val v = selectedVehicle
                    val p = selectedPerson
                    val liters = litersStr.toDoubleOrNull() ?: 0.0
                    val price = pricePerLiterStr.toDoubleOrNull() ?: 0.0
                    val total = totalCostStr.toDoubleOrNull() ?: (liters * price)
                    val km = odometerStr.toDoubleOrNull() ?: 0.0

                    if (v != null && p != null && liters > 0) {
                        val expense = FuelExpenseEntity(
                            vehicleId = v.id,
                            personId = p.id,
                            timestamp = System.currentTimeMillis(),
                            stationName = stationName.trim(),
                            stationAddress = stationAddress.trim(),
                            fuelType = selectedFuelType.id,
                            liters = liters,
                            pricePerLiter = price,
                            totalCostEuros = total,
                            odometerKm = km,
                            isFullTank = isFullTank,
                            notes = notes.trim()
                        )
                        onSave(expense)
                        onDismiss()
                    }
                },
                enabled = selectedVehicle != null && selectedPerson != null && (litersStr.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Guardar Repostaje")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
