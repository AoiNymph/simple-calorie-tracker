package com.macrotracker.app.data

import android.content.Context

data class DaySettings(val dayOfWeek: Int, val calorieGoal: Int, val proteinGoal: Int)

class SettingsManager(context: Context) {
    private val prefs = context.getSharedPreferences("macro_settings", Context.MODE_PRIVATE)

    fun getCalorieGoal(dayOfWeek: Int): Int = prefs.getInt("cal_$dayOfWeek", 2000)
    fun getProteinGoal(dayOfWeek: Int): Int = prefs.getInt("pro_$dayOfWeek", 150)

    fun setGoals(dayOfWeek: Int, calories: Int, protein: Int) {
        prefs.edit()
            .putInt("cal_$dayOfWeek", calories)
            .putInt("pro_$dayOfWeek", protein)
            .apply()
    }

    fun isDarkMode(): Boolean = prefs.getBoolean("dark_mode", false)
    fun setDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean("dark_mode", isDark).apply()
    }

    // NEW: Grabs Monday (1) through Sunday (7) and bundles them into a list
    fun getAllSettings(): List<DaySettings> {
        val list = mutableListOf<DaySettings>()
        for (i in 1..7) {
            list.add(DaySettings(i, getCalorieGoal(i), getProteinGoal(i)))
        }
        return list
    }

    // NEW: Unpacks the list from the JSON and saves it back to the phone
    fun restoreSettings(settingsList: List<DaySettings>) {
        val editor = prefs.edit()
        for (setting in settingsList) {
            editor.putInt("cal_${setting.dayOfWeek}", setting.calorieGoal)
            editor.putInt("pro_${setting.dayOfWeek}", setting.proteinGoal)
        }
        editor.apply()
    }
}