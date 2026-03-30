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

    init {
        loadTodayLog()
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

    // NEW FUNCTION: Updates the daily goals and saves them to the database
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