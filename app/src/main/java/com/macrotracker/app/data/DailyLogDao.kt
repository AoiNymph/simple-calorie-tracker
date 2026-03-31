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

    @Insert
    suspend fun insertFoodEntry(entry: FoodEntry)

    @Delete
    suspend fun deleteFoodEntry(entry: FoodEntry)

    @Query("SELECT * FROM food_entries WHERE date = :date ORDER BY timeAdded DESC")
    fun getFoodEntriesForDate(date: String): Flow<List<FoodEntry>>

    @Query("SELECT * FROM daily_logs")
    suspend fun getAllLogsSync(): List<DailyLog>

    @Query("SELECT * FROM food_entries")
    suspend fun getAllEntriesSync(): List<FoodEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLogs(logs: List<DailyLog>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEntries(entries: List<FoodEntry>)

    @Query("DELETE FROM daily_logs")
    suspend fun wipeLogs()

    @Query("DELETE FROM food_entries")
    suspend fun wipeEntries()

    @Query("SELECT * FROM food_entries WHERE date = :date ORDER BY timeAdded ASC")
    suspend fun getEntriesForDateSync(date: String): List<FoodEntry>
}