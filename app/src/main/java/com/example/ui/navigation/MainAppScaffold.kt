package com.example.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.GasStation
import com.example.ui.MainViewModel
import com.example.ui.screens.calculator.TripCalculatorScreen
import com.example.ui.screens.expenses.ExpensesScreen
import com.example.ui.screens.onboarding.InitialProvinceOnboardingDialog
import com.example.ui.screens.reports.ReportsAndStatsScreen
import com.example.ui.screens.stations.StationsScreen
import com.example.ui.screens.vehicles.VehiclesAndDriversScreen
import kotlinx.coroutines.flow.collectLatest

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Stations : Screen("stations", "GASOLINERAS", Icons.Default.LocalGasStation)
    data object Vehicles : Screen("vehicles", "VEHÍCULOS", Icons.Default.DirectionsCar)
    data object Expenses : Screen("expenses", "GASTOS", Icons.Default.ReceiptLong)
    data object Calculator : Screen("calculator", "CALCULADORA", Icons.Default.Calculate)
    data object Reports : Screen("reports", "INFORMES", Icons.Default.Assessment)
}

@Composable
fun MainAppScaffold(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var prefilledStationForExpense by remember { mutableStateOf<GasStation?>(null) }

    // Request notification permission on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Show price drop notifications or actions via snackbar
    LaunchedEffect(Unit) {
        viewModel.notificationMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
        }
    }

    val screens = listOf(
        Screen.Stations,
        Screen.Vehicles,
        Screen.Expenses,
        Screen.Calculator,
        Screen.Reports
    )


    val screenWidth = LocalConfiguration.current.screenWidthDp
    val isTablet = screenWidth >= 600

    Row(modifier = Modifier.fillMaxSize()) {
        if (isTablet) {
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surface,
                header = {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_ahorragas_logo),
                        contentDescription = "AhorraGAS Logo",
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .size(36.dp)
                    )
                }
            ) {
                screens.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationRailItem(
                        icon = { Icon(imageVector = screen.icon, contentDescription = screen.title) },
                        label = {
                            Text(
                                text = screen.title,
                                fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                softWrap = false
                            )
                        },
                        selected = isSelected,
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (!isTablet) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    ) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp
                        ) {
                            screens.forEach { screen ->
                                val isSelected = currentRoute == screen.route
                                NavigationBarItem(
                                    icon = { Icon(imageVector = screen.icon, contentDescription = screen.title) },
                                    label = {
                                        Text(
                                            text = screen.title,
                                            fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
                                            fontSize = 9.sp,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            softWrap = false
                                        )
                                    },
                                    selected = isSelected,
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    onClick = {
                                        if (currentRoute != screen.route) {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            },
            modifier = Modifier.weight(1f)
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Stations.route,
                modifier = Modifier.padding(innerPadding),
                enterTransition = {
                    fadeIn(animationSpec = tween(280)) + slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(280, easing = FastOutSlowInEasing)
                    )
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(200)) + slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(280, easing = FastOutSlowInEasing)
                    )
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(280)) + slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(280, easing = FastOutSlowInEasing)
                    )
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(200)) + slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(280, easing = FastOutSlowInEasing)
                    )
                }
            ) {
                composable(Screen.Stations.route) {
                    StationsScreen(
                        viewModel = viewModel,
                        onNavigateToAddExpenseWithStation = { station ->
                            prefilledStationForExpense = station
                            navController.navigate(Screen.Expenses.route)
                        }
                    )
                }

                composable(Screen.Calculator.route) {
                    TripCalculatorScreen(viewModel = viewModel)
                }

                composable(Screen.Vehicles.route) {
                    VehiclesAndDriversScreen(viewModel = viewModel)
                }

                composable(Screen.Expenses.route) {
                    ExpensesScreen(
                        viewModel = viewModel,
                        prefilledStationForExpense = prefilledStationForExpense,
                        onClearPrefilledStation = { prefilledStationForExpense = null }
                    )
                }

                composable(Screen.Reports.route) {
                    ReportsAndStatsScreen(viewModel = viewModel)
                }
            }
        }
    }

    // First time onboarding dialog
    if (userSettings.isFirstTimeLaunch) {
        InitialProvinceOnboardingDialog(
            currentProvinceId = userSettings.selectedProvinceId,
            currentIslandId = userSettings.selectedIslandId,
            onConfirm = { provinceId, islandId ->
                viewModel.setProvince(provinceId, islandId)
                viewModel.completeOnboarding()
            }
        )
    }
}
