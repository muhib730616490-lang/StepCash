package com.example.data.database

import androidx.room.*
import com.example.data.model.ShopItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDao {
    @Query("SELECT * FROM shop_items ORDER BY id ASC")
    fun getAllShopItemsFlow(): Flow<List<ShopItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShopItem(item: ShopItem)

    @Update
    suspend fun updateShopItem(item: ShopItem)

    @Query("DELETE FROM shop_items WHERE id = :id")
    suspend fun deleteShopItem(id: Long)

    @Query("DELETE FROM shop_items")
    suspend fun clearAll()
}
