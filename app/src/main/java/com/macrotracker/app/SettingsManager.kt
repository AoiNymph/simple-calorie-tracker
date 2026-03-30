package com.macrotracker.app.data

import android.content.Context

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

    // NEW: Save and load Dark Mode state
    fun isDarkMode(): Boolean = prefs.getBoolean("dark_mode", false)
    fun setDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean("dark_mode", isDark).apply()
    }
}