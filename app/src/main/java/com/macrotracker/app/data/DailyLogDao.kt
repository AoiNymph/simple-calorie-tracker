package com.macrotracker.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLog(log: DailyLog)

    @Query("SELECT * FROM daily_logs WHERE date = :date")
    fun getLogForDate(date: String): Flow<DailyLog?>

    @Query("SELECT * FROM daily_logs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<DailyLog>>

    // NEW: Food Entry Commands
    @Insert
    suspend fun insertFoodEntry(entry: FoodEntry)

    @Delete
    suspend fun deleteFoodEntry(entry: FoodEntry)

    @Query("SELECT * FROM food_entries WHERE date = :date ORDER BY timeAdded DESC")
    fun getFoodEntriesForDate(date: String): Flow<List<FoodEntry>>
}