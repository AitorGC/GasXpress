package com.example.ui.screens.reports

import android.widget.Toast
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PersonEntity
import com.example.data.local.entity.VehicleEntity
import com.example.domain.pdf.PdfReportGenerator
import com.example.ui.MainViewModel
import com.example.ui.components.StatSummaryCard
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsAndStatsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val expenses by viewModel.expenses.collectAsState()
    val vehicles by viewModel.vehicles.collectAsState()
    val persons by viewModel.persons.collectAsState()

    val availableMonths = listOf(
        "Septiembre 2026",
        "Agosto 2026",
        "Julio 2026",
        "Junio 2026",
        "Año Completo 2026"
    )

    var selectedMonth by remember { mutableStateOf(availableMonths.first()) }
    var selectedVehicleFilter by remember { mutableStateOf<VehicleEntity?>(null) }
    var selectedPersonFilter by remember { mutableStateOf<PersonEntity?>(null) }
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }
    var isGeneratingPdf by remember { mutableStateOf(false) }

    val vehicleMap = remember(vehicles) { vehicles.associateBy { it.id } }
    val personMap = remember(persons) { persons.associateBy { it.id } }

    val totalSpent = remember(expenses) { expenses.sumOf { it.totalCostEuros } }
    val totalLiters = remember(expenses) { expenses.sumOf { it.liters } }

    // Grouping for stats
    val expensesByVehicle = remember(expenses) { expenses.groupBy { it.vehicleId } }
    val expensesByPerson = remember(expenses) { expenses.groupBy { it.personId } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Estadísticas e Informes PDF",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. PDF Generation Card (Hero)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Informe Mensual en PDF",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Genera y exporta el desglose oficial por vehículo o familiar",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Period selector chips
                        Text("Período a incluir:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            items(availableMonths) { m ->
                                FilterChip(
                                    selected = selectedMonth == m,
                                    onClick = { selectedMonth = m },
                                    label = { Text(m, fontSize = 11.sp) },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }

                        // Filter by specific vehicle or person for PDF
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Vehicle filter for PDF
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Vehículo:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = selectedVehicleFilter?.name ?: "Todos los vehículos",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }

                            // Person filter for PDF
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Conductor:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = selectedPersonFilter?.name ?: "Toda la familia",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Generate & Share Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    isGeneratingPdf = true
                                    val file = viewModel.generateMonthlyPdfReport(
                                        context = context,
                                        monthYearText = selectedMonth,
                                        targetVehicle = selectedVehicleFilter,
                                        targetPerson = selectedPersonFilter
                                    )
                                    generatedPdfFile = file
                                    isGeneratingPdf = false
                                    if (file != null) {
                                        Toast.makeText(context, "¡Informe PDF generado con éxito!", Toast.LENGTH_SHORT).show()
                                        PdfReportGenerator.viewPdfReport(context, file)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generar PDF", fontWeight = FontWeight.Bold)
                            }

                            if (generatedPdfFile != null) {
                                OutlinedButton(
                                    onClick = {
                                        generatedPdfFile?.let { PdfReportGenerator.sharePdfReport(context, it) }
                                    },
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Share, contentDescription = "Compartir")
                                }
                            }
                        }
                    }
                }
            }

            // 2. Global KPIs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatSummaryCard(
                        title = "Gasto Acumulado",
                        value = String.format(Locale.getDefault(), "%.2f €", totalSpent),
                        subtitle = "${expenses.size} repostajes totales",
                        accentColor = Color(0xFFEA580C),
                        icon = Icons.Default.Euro,
                        modifier = Modifier.weight(1f)
                    )
                    StatSummaryCard(
                        title = "Litros Consumidos",
                        value = String.format(Locale.getDefault(), "%.1f L", totalLiters),
                        subtitle = "${vehicles.size} vehículos en flota",
                        accentColor = Color(0xFF0284C7),
                        icon = Icons.Default.LocalGasStation,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. Breakdown By Vehicle
            item {
                Text(
                    text = "ESTADÍSTICAS POR VEHÍCULO (EUROS Y LITROS)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
            }

            if (vehicles.isEmpty()) {
                item {
                    Text("No hay vehículos registrados.", fontSize = 12.sp, color = Color.Gray)
                }
            } else {
                items(vehicles) { vehicle ->
                    val vExpenses = expensesByVehicle[vehicle.id] ?: emptyList()
                    val vCost = vExpenses.sumOf { it.totalCostEuros }
                    val vLiters = vExpenses.sumOf { it.liters }
                    val percent = if (totalSpent > 0) (vCost / totalSpent).toFloat() else 0f

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(Color(vehicle.colorHex))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${vehicle.name} (${vehicle.licensePlate})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Text(
                                    text = String.format(Locale.getDefault(), "%.2f €", vCost),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = Color(0xFFEA580C)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = { percent },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(vehicle.colorHex),
                                trackColor = Color(0xFFE2E8F0)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${String.format(Locale.getDefault(), "%.1f L", vLiters)} • ${vExpenses.size} repostajes",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${String.format(Locale.getDefault(), "%.1f%%", percent * 100)} del total",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // 4. Breakdown By Person / Driver
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "ESTADÍSTICAS POR CONDUCTOR / FAMILIAR",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
            }

            if (persons.isEmpty()) {
                item {
                    Text("No hay conductores registrados.", fontSize = 12.sp, color = Color.Gray)
                }
            } else {
                items(persons) { person ->
                    val pExpenses = expensesByPerson[person.id] ?: emptyList()
                    val pCost = pExpenses.sumOf { it.totalCostEuros }
                    val pLiters = pExpenses.sumOf { it.liters }
                    val percent = if (totalSpent > 0) (pCost / totalSpent).toFloat() else 0f

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(person.avatarColorHex).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = person.avatarEmoji, fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${person.name} (${person.relationship})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${String.format(Locale.getDefault(), "%.1f L", pLiters)} (${pExpenses.size} repostajes)",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = String.format(Locale.getDefault(), "%.2f €", pCost),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0284C7)
                                )
                                Text(
                                    text = "${String.format(Locale.getDefault(), "%.0f%%", percent * 100)} gasto",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
