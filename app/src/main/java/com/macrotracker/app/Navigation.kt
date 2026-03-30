package com.macrotracker.app

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
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
    val labels = listOf("Main", "History", "Settings")

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

// NEW: The actual Charts/History Screen
@Composable
fun ChartsScreen(viewModel: MacroViewModel = viewModel()) {
    // Grab the list of all past and present logs
    val allLogs by viewModel.allLogs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Macro History",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 24.dp, top = 16.dp)
        )

        if (allLogs.isEmpty()) {
            Text("No data yet. Go track some food on the Main page!", modifier = Modifier.padding(top = 32.dp))
        } else {
            // LazyColumn is a scrolling list that only renders what's on screen
            LazyColumn {
                items(allLogs) { log ->
                    HistoryCard(log = log)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// NEW: The design for a single day's card in the list
@Composable
fun HistoryCard(log: com.macrotracker.app.data.DailyLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = log.date, 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Math to ensure the progress bar doesn't crash if the goal is somehow 0, and caps at 100% (1.0f)
            val calProgress = if (log.calorieGoal > 0) (log.caloriesConsumed.toFloat() / log.calorieGoal.toFloat()).coerceAtMost(1f) else 0f
            Text(text = "Calories: ${log.caloriesConsumed} / ${log.calorieGoal} kcal", style = MaterialTheme.typography.bodyMedium)
            LinearProgressIndicator(
                progress = calProgress, 
                modifier = Modifier.fillMaxWidth().height(8.dp).padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            val proProgress = if (log.proteinGoal > 0) (log.proteinConsumed.toFloat() / log.proteinGoal.toFloat()).coerceAtMost(1f) else 0f
            Text(text = "Protein: ${log.proteinConsumed} / ${log.proteinGoal} g", style = MaterialTheme.typography.bodyMedium)
            LinearProgressIndicator(
                progress = proProgress, 
                modifier = Modifier.fillMaxWidth().height(8.dp).padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun SettingsScreen(viewModel: MacroViewModel = viewModel()) {
    val todayLog by viewModel.todayLog.collectAsState()
    val context = LocalContext.current 
    
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
                val newCals = calorieGoalInput.toIntOrNull() ?: todayLog.calorieGoal
                val newPro = proteinGoalInput.toIntOrNull() ?: todayLog.proteinGoal
                viewModel.updateGoals(newCals, newPro)
                Toast.makeText(context, "Goals Updated!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Goals")
        }
    }
}