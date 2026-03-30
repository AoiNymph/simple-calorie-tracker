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
    
    // Connect to the database we built earlier
    private val dao = AppDatabase.getDatabase(application).dailyLogDao()

    // Get today's date formatted as "YYYY-MM-DD"
    private val todayStr: String
        get() = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    // This StateFlow holds today's data and automatically updates the UI when it changes
    private val _todayLog = MutableStateFlow(DailyLog(date = todayStr))
    val todayLog: StateFlow<DailyLog> = _todayLog.asStateFlow()

    init {
        loadTodayLog()
    }

    private fun loadTodayLog() {
        viewModelScope.launch {
            // Listen to the database for today's log
            dao.getLogForDate(todayStr).collect { log ->
                if (log != null) {
                    _todayLog.value = log // We found today's log, update the UI!
                } else {
                    // It's a new day! Create a fresh log with default goals
                    val newLog = DailyLog(date = todayStr)
                    dao.insertOrUpdateLog(newLog)
                    _todayLog.value = newLog
                }
            }
        }
    }

    // Function to add macros when you hit the button
    fun addMacros(calories: Int, protein: Int) {
        viewModelScope.launch {
            val currentLog = _todayLog.value
            val updatedLog = currentLog.copy(
                caloriesConsumed = currentLog.caloriesConsumed + calories,
                proteinConsumed = currentLog.proteinConsumed + protein
            )
            // Save it back to the database
            dao.insertOrUpdateLog(updatedLog)
        }
    }
}