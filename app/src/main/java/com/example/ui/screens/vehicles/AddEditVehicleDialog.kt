package com.example.ui.screens.vehicles

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PersonEntity
import com.example.data.local.entity.VehicleEntity
import com.example.data.model.FuelType

@Composable
fun AddEditVehicleDialog(
    vehicleToEdit: VehicleEntity? = null,
    availablePersons: List<PersonEntity>,
    onSave: (VehicleEntity) -> Unit,
    onDelete: ((VehicleEntity) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var brand by remember { mutableStateOf(vehicleToEdit?.brand ?: "") }
    var model by remember { mutableStateOf(vehicleToEdit?.model ?: "") }
    var customAlias by remember { mutableStateOf(vehicleToEdit?.name ?: "") }
    var licensePlate by remember { mutableStateOf(vehicleToEdit?.licensePlate ?: "") }
    var selectedFuelType by remember { mutableStateOf(FuelType.fromId(vehicleToEdit?.fuelType)) }
    var consumptionStr by remember { mutableStateOf(vehicleToEdit?.avgConsumptionL100km?.toString() ?: "5.8") }
    var tankCapacityStr by remember { mutableStateOf(vehicleToEdit?.tankCapacityLiters?.toString() ?: "50.0") }
    var initialKmStr by remember { mutableStateOf(vehicleToEdit?.initialOdometerKm?.toInt()?.toString() ?: "0") }
    var assignedPersonId by remember { mutableStateOf(vehicleToEdit?.assignedPersonId) }
    var selectedColor by remember { mutableStateOf(vehicleToEdit?.colorHex ?: 0xFF0284C7) }

    val colors = listOf(0xFF0284C7, 0xFFEA580C, 0xFF10B981, 0xFF8B5CF6, 0xFFEC4899, 0xFF334155)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (vehicleToEdit == null) "Nuevo Vehículo" else "Editar Vehículo",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Brand & Model
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Marca") },
                        placeholder = { Text("ej. SEAT") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("Modelo") },
                        placeholder = { Text("ej. León TDI") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = customAlias,
                    onValueChange = { customAlias = it },
                    label = { Text("Nombre / Alias (ej. Coche de Papá)") },
                    placeholder = { Text(if (brand.isNotBlank() || model.isNotBlank()) "$brand $model".trim() else "Coche Principal") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = licensePlate,
                    onValueChange = { licensePlate = it.uppercase() },
                    label = { Text("Matrícula (ej. 1234-KBC)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Assign to Person
                Text("Asignar a Conductor / Familiar:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = assignedPersonId == null,
                            onClick = { assignedPersonId = null },
                            label = { Text("Sin asignar", fontSize = 11.sp) }
                        )
                    }
                    items(availablePersons) { person ->
                        FilterChip(
                            selected = assignedPersonId == person.id,
                            onClick = { assignedPersonId = person.id },
                            label = { Text("${person.avatarEmoji} ${person.name}", fontSize = 11.sp) }
                        )
                    }
                }

                // Fuel Type
                Text("Tipo de Combustible Principal:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(FuelType.entries) { fuel ->
                        FilterChip(
                            selected = selectedFuelType == fuel,
                            onClick = { selectedFuelType = fuel },
                            label = { Text(fuel.shortName, fontSize = 11.sp) }
                        )
                    }
                }

                // Consumption & Tank
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = consumptionStr,
                        onValueChange = { consumptionStr = it },
                        label = { Text("Consumo") },
                        trailingIcon = { Text("L/100km ", fontSize = 10.sp, color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = tankCapacityStr,
                        onValueChange = { tankCapacityStr = it },
                        label = { Text("Depósito") },
                        trailingIcon = { Text("L ", fontSize = 10.sp, color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Kilometros actuales
                OutlinedTextField(
                    value = initialKmStr,
                    onValueChange = { initialKmStr = it },
                    label = { Text("Kilometraje Actual") },
                    trailingIcon = { Text("km ", fontSize = 10.sp, color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Color
                Text("Color del Vehículo:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(colors) { hex ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(hex))
                                .clickable { selectedColor = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            val resolvedName = if (customAlias.isNotBlank()) {
                customAlias.trim()
            } else if (brand.isNotBlank() || model.isNotBlank()) {
                "$brand $model".trim()
            } else {
                "Mi Vehículo"
            }

            Button(
                onClick = {
                    if (resolvedName.isNotBlank()) {
                        val consumption = consumptionStr.toDoubleOrNull() ?: 6.0
                        val capacity = tankCapacityStr.toDoubleOrNull() ?: 50.0
                        val km = initialKmStr.toDoubleOrNull() ?: 0.0

                        val vehicle = vehicleToEdit?.copy(
                            name = resolvedName,
                            brand = brand.trim(),
                            model = model.trim(),
                            licensePlate = licensePlate.trim().uppercase(),
                            fuelType = selectedFuelType.id,
                            avgConsumptionL100km = consumption,
                            tankCapacityLiters = capacity,
                            assignedPersonId = assignedPersonId,
                            colorHex = selectedColor,
                            initialOdometerKm = km
                        ) ?: VehicleEntity(
                            name = resolvedName,
                            brand = brand.trim(),
                            model = model.trim(),
                            licensePlate = licensePlate.trim().uppercase(),
                            fuelType = selectedFuelType.id,
                            avgConsumptionL100km = consumption,
                            tankCapacityLiters = capacity,
                            assignedPersonId = assignedPersonId,
                            colorHex = selectedColor,
                            initialOdometerKm = km
                        )
                        onSave(vehicle)
                        onDismiss()
                    }
                },
                enabled = brand.isNotBlank() || model.isNotBlank() || customAlias.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Row {
                if (vehicleToEdit != null && onDelete != null) {
                    IconButton(onClick = {
                        onDelete(vehicleToEdit)
                        onDismiss()
                    }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF4444))
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    )
}
