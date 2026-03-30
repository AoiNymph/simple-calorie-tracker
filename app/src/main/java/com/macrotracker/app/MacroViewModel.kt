package com.macrotracker.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.macrotracker.app.data.AppDatabase
import com.macrotracker.app.data.DailyLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MacroViewModel(application: Application) : AndroidViewModel(application) {
    
    private val dao = AppDatabase.getDatabase(application).dailyLogDao()

    private val todayStr: String
        get() = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    private val _todayLog = MutableStateFlow(DailyLog(date = todayStr))
    val todayLog: StateFlow<DailyLog> = _todayLog.asStateFlow()

    // NEW: A flow to hold the entire history of logs
    private val _allLogs = MutableStateFlow<List<DailyLog>>(emptyList())
    val allLogs: StateFlow<List<DailyLog>> = _allLogs.asStateFlow()

    init {
        loadTodayLog()
        loadAllLogs() // NEW: Tell it to load the history when the app starts
    }

    private fun loadTodayLog() {
        viewModelScope.launch {
            dao.getLogForDate(todayStr).collect { log ->
                if (log != null) {
                    _todayLog.value = log 
                } else {
                    val newLog = DailyLog(date = todayStr)
                    dao.insertOrUpdateLog(newLog)
                    _todayLog.value = newLog
                }
            }
        }
    }

    // NEW: Fetch all logs from the database, sorted by date (handled by our DAO query earlier)
    private fun loadAllLogs() {
        viewModelScope.launch {
            dao.getAllLogs().collect { logs ->
                _allLogs.value = logs
            }
        }
    }

    fun addMacros(calories: Int, protein: Int) {
        viewModelScope.launch {
            val currentLog = _todayLog.value
            val updatedLog = currentLog.copy(
                caloriesConsumed = currentLog.caloriesConsumed + calories,
                proteinConsumed = currentLog.proteinConsumed + protein
            )
            dao.insertOrUpdateLog(updatedLog)
        }
    }

    fun updateGoals(newCalorieGoal: Int, newProteinGoal: Int) {
        viewModelScope.launch {
            val currentLog = _todayLog.value
            val updatedLog = currentLog.copy(
                calorieGoal = newCalorieGoal,
                proteinGoal = newProteinGoal
            )
            dao.insertOrUpdateLog(updatedLog)
        }
    }
}