package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val photoUrl: String,
    val steps: Int = 0,
    val distance: Float = 0f, // in km
    val points: Int = 0,
    val balance: Double = 0.0, // in USD
    val level: Int = 1,
    val streak: Int = 1,
    val isAdmin: Boolean = false,
    val isLoggedIn: Boolean = true,
    val securityPin: String = ""
)
