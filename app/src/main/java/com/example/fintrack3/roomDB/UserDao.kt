package com.example.fintrack3.roomDB

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface UserDao {

    //Get All User
    @Query("SELECT * FROM user_table")
    fun getAllUsers() : List<User>

    //Add new User
    @Upsert
    fun addUser(user: User)

    //Delete User using User ID
    @Query("Delete FROM user_table WHERE userId = :id")
    fun deleteUser(id : Int)

    //Get User info using email
    @Query("SELECT * FROM user_table WHERE email = :email")
    fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM user_table WHERE userId = :userId")
    suspend fun getUserById(userId: Long): User?
}