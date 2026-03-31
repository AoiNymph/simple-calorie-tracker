package com.macrotracker.app

import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.macrotracker.app.data.AppDatabase
import com.macrotracker.app.data.DailyLog
import com.macrotracker.app.data.FoodEntry
import com.macrotracker.app.data.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class BackupData(val logs: List<DailyLog>, val entries: List<FoodEntry>)

class MacroViewModel(application: Application) : AndroidViewModel(application) {
    
    private val dao = AppDatabase.getDatabase(application).dailyLogDao()
    val settings = SettingsManager(application) 

    private val todayDate = LocalDate.now()
    private val todayStr: String get() = todayDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
    private val currentDayOfWeek: Int get() = todayDate.dayOfWeek.value 

    private val _todayLog = MutableStateFlow(DailyLog(date = todayStr))
    val todayLog: StateFlow<DailyLog> = _todayLog.asStateFlow()

    private val _allLogs = MutableStateFlow<List<DailyLog>>(emptyList())
    val allLogs: StateFlow<List<DailyLog>> = _allLogs.asStateFlow()

    private val _todayEntries = MutableStateFlow<List<FoodEntry>>(emptyList())
    val todayEntries: StateFlow<List<FoodEntry>> = _todayEntries.asStateFlow()

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
                    val newLog = DailyLog(date = todayStr, calorieGoal = settings.getCalorieGoal(currentDayOfWeek), proteinGoal = settings.getProteinGoal(currentDayOfWeek))
                    dao.insertOrUpdateLog(newLog)
                    _todayLog.value = newLog
                }
            }
        }
    }

    private fun loadAllLogs() { viewModelScope.launch { dao.getAllLogs().collect { _allLogs.value = it } } }
    private fun loadTodayEntries() { viewModelScope.launch { dao.getFoodEntriesForDate(todayStr).collect { _todayEntries.value = it } } }

    fun addMacros(calories: Int, protein: Int, name: String) {
        viewModelScope.launch {
            val entry = FoodEntry(date = todayStr, calories = calories, protein = protein, name = name)
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
        viewModelScope.launch { dao.insertOrUpdateLog(_todayLog.value.copy(calorieGoal = newCalorieGoal, proteinGoal = newProteinGoal)) }
    }

    fun setDarkMode(isDark: Boolean) {
        settings.setDarkMode(isDark)
        _isDarkMode.value = isDark
    }

    suspend fun getEntriesForDate(date: String): List<FoodEntry> {
        return dao.getEntriesForDateSync(date)
    }

    fun exportData(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val backup = BackupData(dao.getAllLogsSync(), dao.getAllEntriesSync())
                val jsonString = Gson().toJson(backup)
                context.contentResolver.openOutputStream(uri)?.use { it.write(jsonString.toByteArray()) }
                launch(Dispatchers.Main) { Toast.makeText(context, "Export Successful!", Toast.LENGTH_SHORT).show() }
            } catch (e: Exception) {
                launch(Dispatchers.Main) { Toast.makeText(context, "Export Failed", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    fun importData(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val jsonString = inputStream.bufferedReader().readText()
                    val backup = Gson().fromJson(jsonString, BackupData::class.java)
                    
                    dao.wipeEntries()
                    dao.wipeLogs()
                    dao.insertAllLogs(backup.logs)
                    dao.insertAllEntries(backup.entries)
                    
                    launch(Dispatchers.Main) { Toast.makeText(context, "Import Successful!", Toast.LENGTH_SHORT).show() }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) { Toast.makeText(context, "Import Failed. Invalid file.", Toast.LENGTH_SHORT).show() }
            }
        }
    }
}