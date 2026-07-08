package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "step_records")
data class StepRecord(
    @PrimaryKey val date: String, // format: YYYY-MM-DD
    val steps: Int,
    val distance: Float, // in km
    val pointsEarned: Int,
    val calories: Float
)
