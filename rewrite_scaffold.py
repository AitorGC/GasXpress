import re

with open("app/src/main/java/com/example/ui/navigation/MainAppScaffold.kt", "r") as f:
    content = f.read()

# Add import
if "import androidx.compose.ui.platform.LocalConfiguration" not in content:
    content = content.replace(
        "import androidx.compose.ui.platform.LocalContext",
        "import androidx.compose.ui.platform.LocalContext\nimport androidx.compose.ui.platform.LocalConfiguration\nimport androidx.compose.foundation.layout.Row\nimport androidx.compose.foundation.layout.RowScope"
    )

# Replace the Scaffold part
# Look for Scaffold( ... ) { innerPadding -> ... }
scaffold_pattern = r"    Scaffold\([\s\S]+?    // First time onboarding dialog"

new_scaffold_code = """
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val isTablet = screenWidth >= 600

    Row(modifier = Modifier.fillMaxSize()) {
        if (isTablet) {
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surface,
                header = {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "App Logo",
                        modifier = Modifier.padding(vertical = 12.dp)
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

    // First time onboarding dialog"""

new_content = re.sub(scaffold_pattern, new_scaffold_code, content)

with open("app/src/main/java/com/example/ui/navigation/MainAppScaffold.kt", "w") as f:
    f.write(new_content)

print("Scaffold replaced!")
