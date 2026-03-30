package com.macrotracker.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_logs")
data class DailyLog(
    @PrimaryKey 
    val date: String, 
    val calorieGoal: Int = 2000, 
    val proteinGoal: Int = 150,
    val caloriesConsumed: Int = 0,
    val proteinConsumed: Int = 0
)