package com.example.ui.screens.onboarding

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.model.SpanishIsland
import com.example.data.model.SpanishProvinces
import com.example.util.LocationHelper
import kotlinx.coroutines.launch

@Composable
fun InitialProvinceOnboardingDialog(
    currentProvinceId: String,
    currentIslandId: String? = null,
    onConfirm: (provinceId: String, islandId: String?) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedProvinceId by remember { mutableStateOf(currentProvinceId) }
    var selectedIslandId by remember { mutableStateOf(currentIslandId) }
    var searchQuery by remember { mutableStateOf("") }
    var isDetectingLocation by remember { mutableStateOf(false) }

    var expandedProvinceId by remember {
        mutableStateOf<String?>(
            if (SpanishProvinces.isArchipelagoProvince(currentProvinceId)) currentProvinceId else null
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            coroutineScope.launch {
                isDetectingLocation = true
                try {
                    val loc = LocationHelper.requestSingleLocationUpdate(context)
                    if (loc != null) {
                        val (prov, island) = LocationHelper.resolveProvinceAndIsland(context, loc)
                        selectedProvinceId = prov.id
                        selectedIslandId = island?.id
                        if (SpanishProvinces.isArchipelagoProvince(prov.id)) {
                            expandedProvinceId = prov.id
                        }
                    }
                } finally {
                    isDetectingLocation = false
                }
            }
        }
    }

    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) SpanishProvinces.list
        else {
            val q = searchQuery.trim().lowercase()
            SpanishProvinces.list.filter { prov ->
                prov.name.lowercase().contains(q) ||
                prov.ccaa.lowercase().contains(q) ||
                SpanishProvinces.getIslandsForProvince(prov.id).any { island ->
                    island.name.lowercase().contains(q) ||
                    island.municipalities.any { it.lowercase().contains(q) }
                }
            }
        }
    }

    Dialog(
        onDismissRequest = { /* mandatory on first run */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalGasStation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "¡Bienvenido a GAS GAS!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Selecciona tu provincia o isla para consultar precios oficiales MITECO en tiempo real:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // GPS Auto-detect Button
                FilledTonalButton(
                    onClick = {
                        val hasFine = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        val hasCoarse = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasFine || hasCoarse) {
                            coroutineScope.launch {
                                isDetectingLocation = true
                                try {
                                    val loc = LocationHelper.requestSingleLocationUpdate(context)
                                    if (loc != null) {
                                        val (prov, island) = LocationHelper.resolveProvinceAndIsland(context, loc)
                                        selectedProvinceId = prov.id
                                        selectedIslandId = island?.id
                                        if (SpanishProvinces.isArchipelagoProvince(prov.id)) {
                                            expandedProvinceId = prov.id
                                        }
                                    }
                                } finally {
                                    isDetectingLocation = false
                                }
                            }
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isDetectingLocation
                ) {
                    if (isDetectingLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Detectando ubicación GPS...", fontSize = 12.sp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Detectar mi ubicación con GPS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("O busca provincia o isla...", fontSize = 12.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Limpiar", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                ) {
                    items(filtered, key = { it.id }) { prov ->
                        val isArchipelago = SpanishProvinces.isArchipelagoProvince(prov.id)
                        val islands = remember(prov.id) { SpanishProvinces.getIslandsForProvince(prov.id) }
                        val isExpanded = isArchipelago && (expandedProvinceId == prov.id || searchQuery.isNotBlank())
                        val isProvSelected = prov.id == selectedProvinceId && selectedIslandId == null

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isArchipelago) {
                                            expandedProvinceId = if (expandedProvinceId == prov.id) null else prov.id
                                        } else {
                                            selectedProvinceId = prov.id
                                            selectedIslandId = null
                                        }
                                    },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isProvSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 7.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = prov.name,
                                                fontWeight = if (isProvSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 13.sp
                                            )
                                            if (isArchipelago) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "🏝️ Islas",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        Text(
                                            text = prov.ccaa,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isArchipelago) {
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    } else if (isProvSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            // Archipelago islands
                            AnimatedVisibility(
                                visible = isArchipelago && isExpanded,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                Column(modifier = Modifier.padding(start = 14.dp, bottom = 4.dp)) {
                                    // Whole province option
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedProvinceId = prov.id
                                                selectedIslandId = null
                                            },
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isProvSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent
                                    ) {
                                        Text(
                                            text = "📍 Toda la provincia (${prov.name})",
                                            fontSize = 12.sp,
                                            fontWeight = if (isProvSelected) FontWeight.Bold else FontWeight.Normal,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                        )
                                    }

                                    islands.forEach { island ->
                                        val isIslandSelected = prov.id == selectedProvinceId && selectedIslandId == island.id
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                selectedProvinceId = prov.id
                                                selectedIslandId = island.id
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isIslandSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "🏝️ ${island.name}",
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isIslandSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isIslandSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                                )
                                                if (isIslandSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val buttonLabel = remember(selectedProvinceId, selectedIslandId) {
                    val island = SpanishProvinces.findIslandById(selectedIslandId)
                    if (island != null) {
                        "Comenzar en ${island.name}"
                    } else {
                        "Comenzar con ${SpanishProvinces.findById(selectedProvinceId).name}"
                    }
                }

                Button(
                    onClick = { onConfirm(selectedProvinceId, selectedIslandId) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(buttonLabel, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
