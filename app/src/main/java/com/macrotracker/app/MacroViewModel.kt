package com.macrotracker.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.macrotracker.app.data.AppDatabase
import com.macrotracker.app.data.DailyLog
import com.macrotracker.app.data.FoodEntry
import com.macrotracker.app.data.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MacroViewModel(application: Application) : AndroidViewModel(application) {
    
    private val dao = AppDatabase.getDatabase(application).dailyLogDao()
    val settings = SettingsManager(application) 

    private val todayDate = LocalDate.now()
    private val todayStr: String
        get() = todayDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
    private val currentDayOfWeek: Int
        get() = todayDate.dayOfWeek.value 

    private val _todayLog = MutableStateFlow(DailyLog(date = todayStr))
    val todayLog: StateFlow<DailyLog> = _todayLog.asStateFlow()

    private val _allLogs = MutableStateFlow<List<DailyLog>>(emptyList())
    val allLogs: StateFlow<List<DailyLog>> = _allLogs.asStateFlow()

    private val _todayEntries = MutableStateFlow<List<FoodEntry>>(emptyList())
    val todayEntries: StateFlow<List<FoodEntry>> = _todayEntries.asStateFlow()

    // NEW: Dark mode state
    private val _isDarkMode = MutableStateFlow(settings.isDarkMode())
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    init {
        loadTodayLog()
        loadAllLogs()
        loadTodayEntries()
    }

    private fun loadTodayLog() {
        viewModelScope.launch {
            dao.getLogForDate(todayStr).collect { log ->
                if (log != null) {
                    _todayLog.value = log 
                } else {
                    val newLog = DailyLog(
                        date = todayStr,
                        calorieGoal = settings.getCalorieGoal(currentDayOfWeek),
                        proteinGoal = settings.getProteinGoal(currentDayOfWeek)
                    )
                    dao.insertOrUpdateLog(newLog)
                    _todayLog.value = newLog
                }
            }
        }
    }

    private fun loadAllLogs() {
        viewModelScope.launch { dao.getAllLogs().collect { _allLogs.value = it } }
    }

    private fun loadTodayEntries() {
        viewModelScope.launch { dao.getFoodEntriesForDate(todayStr).collect { _todayEntries.value = it } }
    }

    fun addMacros(calories: Int, protein: Int) {
        viewModelScope.launch {
            val entry = FoodEntry(date = todayStr, calories = calories, protein = protein)
            dao.insertFoodEntry(entry)

            val currentLog = _todayLog.value
            dao.insertOrUpdateLog(currentLog.copy(
                caloriesConsumed = currentLog.caloriesConsumed + calories,
                proteinConsumed = currentLog.proteinConsumed + protein
            ))
        }
    }

    fun deleteEntry(entry: FoodEntry) {
        viewModelScope.launch {
            dao.deleteFoodEntry(entry)
            val currentLog = _todayLog.value
            dao.insertOrUpdateLog(currentLog.copy(
                caloriesConsumed = currentLog.caloriesConsumed - entry.calories,
                proteinConsumed = currentLog.proteinConsumed - entry.protein
            ))
        }
    }

    fun updateTodayGoals(newCalorieGoal: Int, newProteinGoal: Int) {
        viewModelScope.launch {
            dao.insertOrUpdateLog(_todayLog.value.copy(calorieGoal = newCalorieGoal, proteinGoal = newProteinGoal))
        }
    }

    // NEW: Toggle Dark Mode
    fun setDarkMode(isDark: Boolean) {
        settings.setDarkMode(isDark)
        _isDarkMode.value = isDark
    }
}