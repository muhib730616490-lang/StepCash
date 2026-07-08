package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titleEn: String,
    val titleAr: String,
    val points: Int,
    val isCompleted: Boolean = false,
    val type: String, // "DAILY", "CHALLENGE", "REFERRAL"
    val targetSteps: Int = 0,
    val currentProgress: Int = 0
)
