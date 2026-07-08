package com.example.data.database

import androidx.room.*
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun getLoggedInUserFlow(): Flow<User?>

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getLoggedInUser(): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): User?

    @Query("SELECT * FROM users ORDER BY steps DESC")
    fun getAllUsersFlow(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE users SET isLoggedIn = 0")
    suspend fun logoutAll()

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: String)
}
