package com.example.ui.screens.vehicles

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PersonEntity
import com.example.data.local.entity.VehicleEntity
import com.example.ui.MainViewModel
import com.example.ui.components.VehicleCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiclesAndDriversScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    val persons by viewModel.persons.collectAsStateWithLifecycle()

    var showAddVehicleDialog by remember { mutableStateOf(false) }
    var vehicleToEdit by remember { mutableStateOf<VehicleEntity?>(null) }

    var showAddPersonDialog by remember { mutableStateOf(false) }
    var personToEdit by remember { mutableStateOf<PersonEntity?>(null) }

    val personMap = remember(persons) { persons.associateBy { it.id } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Vehículos y Familia",
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                windowInsets = WindowInsets(0.dp),
                actions = {
                    IconButton(onClick = { showAddPersonDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Añadir conductor",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddVehicleDialog = true },
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
                text = { Text("Añadir Vehículo") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Family / Drivers Section Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "CONDUCTORES Y FAMILIA",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Asigna vehículos a miembros familiares (Papá, Mamá, Hijos...)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 2. Family Members Horizontal Row
            item {
                if (persons.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAddPersonDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Pulsa aquí o en el botón superior para añadir un conductor.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(persons) { person ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 1.dp,
                                modifier = Modifier.clickable { personToEdit = person }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(person.avatarColorHex).copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = person.avatarEmoji, fontSize = 16.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = person.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = person.relationship,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Vehicles Section Header
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GARAJE DE VEHÍCULOS (${vehicles.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // 4. Vehicles List
            if (vehicles.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No tienes vehículos guardados",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Añade el coche familiar o de cada persona para controlar consumos y gastos.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(vehicles, key = { it.id }) { vehicle ->
                    val assignedPerson = vehicle.assignedPersonId?.let { personMap[it] }
                    VehicleCard(
                        vehicle = vehicle,
                        assignedPerson = assignedPerson,
                        onEdit = { vehicleToEdit = vehicle }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Add Vehicle Dialog
    if (showAddVehicleDialog) {
        AddEditVehicleDialog(
            vehicleToEdit = null,
            availablePersons = persons,
            onSave = { viewModel.addVehicle(it) },
            onDismiss = { showAddVehicleDialog = false }
        )
    }

    // Edit Vehicle Dialog
    vehicleToEdit?.let { v ->
        AddEditVehicleDialog(
            vehicleToEdit = v,
            availablePersons = persons,
            onSave = { viewModel.updateVehicle(it) },
            onDelete = { viewModel.deleteVehicle(it) },
            onDismiss = { vehicleToEdit = null }
        )
    }

    // Add Person Dialog
    if (showAddPersonDialog) {
        AddEditPersonDialog(
            personToEdit = null,
            onSave = { viewModel.addPerson(it) },
            onDismiss = { showAddPersonDialog = false }
        )
    }

    // Edit Person Dialog
    personToEdit?.let { p ->
        AddEditPersonDialog(
            personToEdit = p,
            onSave = { viewModel.updatePerson(it) },
            onDelete = { viewModel.deletePerson(it) },
            onDismiss = { personToEdit = null }
        )
    }
}
