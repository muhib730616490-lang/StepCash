package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaction_records")
data class TransactionRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val type: String, // "WITHDRAWAL", "DEPOSIT", "REWARD", "SHOP_PURCHASE"
    val amount: Double, // in USD
    val points: Int, // associated points
    val status: String, // "PENDING", "COMPLETED", "FAILED"
    val destinationAddress: String, // Crypto wallet or phone number
    val paymentMethod: String, // "USDT (TRC-20)", "Binance Pay", "PayPal"
    val timestamp: Long = System.currentTimeMillis()
)
