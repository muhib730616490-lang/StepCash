package com.example.data.database

import androidx.room.*
import com.example.data.model.TaskRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY id ASC")
    fun getAllTasksFlow(): Flow<List<TaskRecord>>

    @Query("SELECT * FROM tasks WHERE type = :type ORDER BY id ASC")
    fun getTasksByTypeFlow(type: String): Flow<List<TaskRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskRecord)

    @Update
    suspend fun updateTask(task: TaskRecord)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)

    @Query("DELETE FROM tasks")
    suspend fun clearAllTasks()
}
