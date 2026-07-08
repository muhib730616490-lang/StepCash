package com.example.data.database

import androidx.room.*
import com.example.data.model.TransactionRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transaction_records ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionRecord)

    @Update
    suspend fun updateTransaction(transaction: TransactionRecord)

    @Query("DELETE FROM transaction_records WHERE id = :id")
    suspend fun deleteTransaction(id: Long)
}
