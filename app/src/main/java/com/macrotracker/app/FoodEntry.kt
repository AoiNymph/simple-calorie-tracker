package com.macrotracker.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_entries")
data class FoodEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val calories: Int,
    val protein: Int,
    val name: String = "",
    val timeAdded: Long = System.currentTimeMillis()
)