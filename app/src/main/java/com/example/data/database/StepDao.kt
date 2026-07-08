package com.example.data.database

import androidx.room.*
import com.example.data.model.StepRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Query("SELECT * FROM step_records ORDER BY date DESC")
    fun getAllStepRecords(): Flow<List<StepRecord>>

    @Query("SELECT * FROM step_records WHERE date = :date LIMIT 1")
    suspend fun getStepRecordByDate(date: String): StepRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStepRecord(record: StepRecord)

    @Query("DELETE FROM step_records")
    suspend fun clearAll()
}
