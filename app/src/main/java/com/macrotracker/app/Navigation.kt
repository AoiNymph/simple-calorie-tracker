package com.macrotracker.app

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import java.time.LocalDate

@Composable
fun AppNavigation(viewModel: MacroViewModel) {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen(viewModel) }
            composable("charts") { ChartsScreen(viewModel) }
            composable("settings") { SettingsScreen(viewModel) }
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

@Composable
fun HomeScreen(viewModel: MacroViewModel) {
    val todayLog by viewModel.todayLog.collectAsState()
    val todayEntries by viewModel.todayEntries.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Today's Macros", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        
        // Calorie Card with Progress Bar
        Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                val calRatio = if (todayLog.calorieGoal > 0) todayLog.caloriesConsumed.toFloat() / todayLog.calorieGoal.toFloat() else 0f
                Text("Calories: ${todayLog.caloriesConsumed} / ${todayLog.calorieGoal} kcal", style = MaterialTheme.typography.titleMedium)
                LinearProgressIndicator(
                    progress = calRatio.coerceAtMost(1f), 
                    color = getProgressColor(calRatio),
                    modifier = Modifier.fillMaxWidth().height(12.dp).padding(top = 8.dp)
                )
            }
        }

        // Protein Card with Progress Bar
        Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                val proRatio = if (todayLog.proteinGoal > 0) todayLog.proteinConsumed.toFloat() / todayLog.proteinGoal.toFloat() else 0f
                Text("Protein: ${todayLog.proteinConsumed} / ${todayLog.proteinGoal} g", style = MaterialTheme.typography.titleMedium)
                LinearProgressIndicator(
                    progress = proRatio.coerceAtMost(1f), 
                    color = getProgressColor(proRatio),
                    modifier = Modifier.fillMaxWidth().height(12.dp).padding(top = 8.dp)
                )
            }
        }

        Button(onClick = { showDialog = true }, modifier = Modifier.fillMaxWidth(0.6f).padding(vertical = 16.dp)) {
            Text("Add Food")
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))
        Text("Today's Entries", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(todayEntries) { entry ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("+${entry.calories} kcal  |  +${entry.protein} g", fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = { viewModel.deleteEntry(entry) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        var caloriesInput by remember { mutableStateOf("") }
        var proteinInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add Macros") },
            text = {
                Column {
                    OutlinedTextField(value = caloriesInput, onValueChange = { caloriesInput = it }, label = { Text("Calories (kcal)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = proteinInput, onValueChange = { proteinInput = it }, label = { Text("Protein (g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addMacros(caloriesInput.toIntOrNull() ?: 0, proteinInput.toIntOrNull() ?: 0)
                    showDialog = false
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun ChartsScreen(viewModel: MacroViewModel) {
    val allLogs by viewModel.allLogs.collectAsState()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Macro History", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(bottom = 24.dp, top = 16.dp))
        if (allLogs.isEmpty()) {
            Text("No data yet.", modifier = Modifier.padding(top = 32.dp))
        } else {
            LazyColumn {
                items(allLogs) { log ->
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(log.date, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            val calRatio = if (log.calorieGoal > 0) log.caloriesConsumed.toFloat() / log.calorieGoal.toFloat() else 0f
                            Text("Calories: ${log.caloriesConsumed} / ${log.calorieGoal} kcal", style = MaterialTheme.typography.bodyMedium)
                            LinearProgressIndicator(progress = calRatio.coerceAtMost(1f), color = getProgressColor(calRatio), modifier = Modifier.fillMaxWidth().height(8.dp).padding(top = 4.dp))
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            val proRatio = if (log.proteinGoal > 0) log.proteinConsumed.toFloat() / log.proteinGoal.toFloat() else 0f
                            Text("Protein: ${log.proteinConsumed} / ${log.proteinGoal} g", style = MaterialTheme.typography.bodyMedium)
                            LinearProgressIndicator(progress = proRatio.coerceAtMost(1f), color = getProgressColor(proRatio), modifier = Modifier.fillMaxWidth().height(8.dp).padding(top = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MacroViewModel) {
    val context = LocalContext.current 
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    var selectedDay by remember { mutableStateOf(LocalDate.now().dayOfWeek.value) }
    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    var expanded by remember { mutableStateOf(false) }

    var calorieGoalInput by remember(selectedDay) { mutableStateOf(viewModel.settings.getCalorieGoal(selectedDay).toString()) }
    var proteinGoalInput by remember(selectedDay) { mutableStateOf(viewModel.settings.getProteinGoal(selectedDay).toString()) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("App Settings", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(bottom = 24.dp))

        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Dark Mode", style = MaterialTheme.typography.titleMedium)
                Switch(checked = isDarkMode, onCheckedChange = { viewModel.setDarkMode(it) })
            }
        }

        Divider(modifier = Modifier.padding(bottom = 24.dp))
        Text("Daily Goals", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = daysOfWeek[selectedDay - 1],
                onValueChange = {}, readOnly = true, label = { Text("Select Day") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                daysOfWeek.forEachIndexed { index, day ->
                    DropdownMenuItem(text = { Text(day) }, onClick = { selectedDay = index + 1; expanded = false })
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = calorieGoalInput, onValueChange = { calorieGoalInput = it }, label = { Text("Calorie Goal (kcal)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = proteinGoalInput, onValueChange = { proteinGoalInput = it }, label = { Text("Protein Goal (g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val newCals = calorieGoalInput.toIntOrNull() ?: 2000
                val newPro = proteinGoalInput.toIntOrNull() ?: 150
                viewModel.settings.setGoals(selectedDay, newCals, newPro)
                if (selectedDay == LocalDate.now().dayOfWeek.value) viewModel.updateTodayGoals(newCals, newPro)
                Toast.makeText(context, "Saved for ${daysOfWeek[selectedDay - 1]}!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Daily Goals")
        }
    }
}

// NEW: The logic that determines the color of the progress bar based on your requested percentages
@Composable
fun getProgressColor(progress: Float): Color {
    return when {
        progress <= 0.50f -> Color(0xFF4CAF50) // Green
        progress <= 0.70f -> Color(0xFFFFEB3B) // Yellow
        progress <= 0.85f -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336) // Red
    }
}