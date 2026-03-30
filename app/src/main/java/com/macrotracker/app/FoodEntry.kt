package com.macrotracker.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_entries")
data class FoodEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // Which day this belongs to
    val calories: Int,
    val protein: Int,
    val timeAdded: Long = System.currentTimeMillis() // To sort them by when you ate them
)