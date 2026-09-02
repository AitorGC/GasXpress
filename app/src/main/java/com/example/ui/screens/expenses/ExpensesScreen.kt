package com.example.ui.screens.expenses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.FuelExpenseEntity
import com.example.data.model.GasStation
import com.example.ui.MainViewModel
import com.example.ui.components.ExpenseItemCard
import com.example.ui.components.StatSummaryCard
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    viewModel: MainViewModel,
    prefilledStationForExpense: GasStation? = null,
    onClearPrefilledStation: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val expenses by viewModel.expenses.collectAsState()
    val vehicles by viewModel.vehicles.collectAsState()
    val persons by viewModel.persons.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var activeVehicleFilterId by remember { mutableStateOf<Long?>(null) }
    var activePersonFilterId by remember { mutableStateOf<Long?>(null) }

    val vehicleMap = remember(vehicles) { vehicles.associateBy { it.id } }
    val personMap = remember(persons) { persons.associateBy { it.id } }

    // Check if opened with prefilled station
    LaunchedEffect(prefilledStationForExpense) {
        if (prefilledStationForExpense != null) {
            showAddDialog = true
        }
    }

    val filteredExpenses = remember(expenses, activeVehicleFilterId, activePersonFilterId) {
        expenses.filter { exp ->
            val matchV = activeVehicleFilterId == null || exp.vehicleId == activeVehicleFilterId
            val matchP = activePersonFilterId == null || exp.personId == activePersonFilterId
            matchV && matchP
        }
    }

    val totalSpent = remember(filteredExpenses) { filteredExpenses.sumOf { it.totalCostEuros } }
    val totalLiters = remember(filteredExpenses) { filteredExpenses.sumOf { it.liters } }
    val avgPrice = remember(totalSpent, totalLiters) { if (totalLiters > 0) totalSpent / totalLiters else 0.0 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Historial de Gastos",
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                windowInsets = WindowInsets(0.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
                text = { Text("Nuevo Repostaje") },
                containerColor = Color(0xFFEA580C),
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. KPI Summary Banner
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatSummaryCard(
                        title = "Gasto Total",
                        value = String.format(Locale.getDefault(), "%.2f €", totalSpent),
                        subtitle = "${filteredExpenses.size} repostajes",
                        accentColor = Color(0xFFEA580C),
                        modifier = Modifier.weight(1f)
                    )
                    StatSummaryCard(
                        title = "Litros Totales",
                        value = String.format(Locale.getDefault(), "%.1f L", totalLiters),
                        subtitle = "Med: ${String.format(Locale.getDefault(), "%.3f €/L", avgPrice)}",
                        accentColor = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. Filter by Vehicle & Driver Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Filtrar por Vehículo o Familiar:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            FilterChip(
                                selected = activeVehicleFilterId == null && activePersonFilterId == null,
                                onClick = {
                                    activeVehicleFilterId = null
                                    activePersonFilterId = null
                                },
                                label = { Text("Todos", fontSize = 11.sp) }
                            )
                        }

                        items(vehicles) { v ->
                            FilterChip(
                                selected = activeVehicleFilterId == v.id,
                                onClick = {
                                    activeVehicleFilterId = if (activeVehicleFilterId == v.id) null else v.id
                                },
                                label = { Text(v.name, fontSize = 11.sp) }
                            )
                        }

                        items(persons) { p ->
                            FilterChip(
                                selected = activePersonFilterId == p.id,
                                onClick = {
                                    activePersonFilterId = if (activePersonFilterId == p.id) null else p.id
                                },
                                label = { Text("${p.avatarEmoji} ${p.name}", fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            // 3. Expenses List
            if (filteredExpenses.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Sin repostajes registrados",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Pulsa 'Nuevo Repostaje' para registrar tus tickets de gasolina.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredExpenses, key = { it.id }) { expense ->
                    ExpenseItemCard(
                        expense = expense,
                        vehicle = vehicleMap[expense.vehicleId],
                        person = personMap[expense.personId],
                        onDelete = { viewModel.deleteExpense(expense) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            prefilledStation = prefilledStationForExpense,
            vehicles = vehicles,
            persons = persons,
            onSave = { expense ->
                viewModel.addExpense(expense)
                onClearPrefilledStation()
            },
            onDismiss = {
                showAddDialog = false
                onClearPrefilledStation()
            }
        )
    }
}
