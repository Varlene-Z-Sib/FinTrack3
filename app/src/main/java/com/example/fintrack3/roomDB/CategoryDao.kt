package com.example.fintrack3.roomDB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CategoryDao {
    @Insert
    suspend fun insert(category: Category)

    @Query("SELECT * FROM category_table")
    suspend fun getAllCategories(): List<Category>

    @Query("SELECT * FROM category_table WHERE user_id = :userId")
    suspend fun getCategoriesByUser(userId: String): List<Category>
}
