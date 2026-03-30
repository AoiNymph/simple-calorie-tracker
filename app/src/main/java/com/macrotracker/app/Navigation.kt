package com.macrotracker.app

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen() }
            composable("charts") { ChartsScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf("home", "charts", "settings")
    val icons = listOf(Icons.Default.Home, Icons.Default.DateRange, Icons.Default.Settings)
    val labels = listOf("Main", "Charts", "Settings")

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEachIndexed { index, route ->
            NavigationBarItem(
                icon = { Icon(icons[index], contentDescription = labels[index]) },
                label = { Text(labels[index]) },
                selected = currentRoute == route,
                onClick = {
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun ChartsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Charts & Calendar Coming Soon!", style = MaterialTheme.typography.headlineMedium)
    }
}

// NEW FUNCTION: The actual Settings Screen
@Composable
fun SettingsScreen(viewModel: MacroViewModel = viewModel()) {
    // 1. Get the current data so we know what to show in the text boxes initially
    val todayLog by viewModel.todayLog.collectAsState()
    val context = LocalContext.current // Used to show a little "Saved!" toast message
    
    // 2. State variables for what the user is typing
    // We use "remember(todayLog.calorieGoal)" so it updates if the database changes
    var calorieGoalInput by remember(todayLog.calorieGoal) { mutableStateOf(todayLog.calorieGoal.toString()) }
    var proteinGoalInput by remember(todayLog.proteinGoal) { mutableStateOf(todayLog.proteinGoal.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Daily Goals",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp, top = 16.dp)
        )

        OutlinedTextField(
            value = calorieGoalInput,
            onValueChange = { calorieGoalInput = it },
            label = { Text("Daily Calorie Goal (kcal)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = proteinGoalInput,
            onValueChange = { proteinGoalInput = it },
            label = { Text("Daily Protein Goal (g)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                // Convert text to numbers, falling back to current goals if they type gibberish
                val newCals = calorieGoalInput.toIntOrNull() ?: todayLog.calorieGoal
                val newPro = proteinGoalInput.toIntOrNull() ?: todayLog.proteinGoal
                
                // Tell the ViewModel to save it
                viewModel.updateGoals(newCals, newPro)
                
                // Show a little popup message confirming the save
                Toast.makeText(context, "Goals Updated!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Goals")
        }
    }
}