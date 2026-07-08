package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shop_items")
data class ShopItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titleEn: String,
    val titleAr: String,
    val descriptionEn: String,
    val descriptionAr: String,
    val pricePoints: Int,
    val isVip: Boolean = false,
    val iconName: String, // e.g., "star", "speed", "flash_on"
    val purchased: Boolean = false
)
