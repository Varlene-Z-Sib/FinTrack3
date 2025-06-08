package com.example.myapplication.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction): Int

    @Query("SELECT * FROM transaction_table WHERE transactionId = :transactionId")
    suspend fun getTransactionById(transactionId: Long): Transaction?

    @Query("DELETE FROM transaction_table WHERE transactionId = :transactionId")
    suspend fun deleteTransactionById(transactionId: Long): Int

    @Query("SELECT * FROM transaction_table WHERE user_Id = :userId ORDER BY transaction_date DESC")
    suspend fun getAllUserTransactions(userId: String): List<Transaction>

    @Query("SELECT SUM(amount) FROM transaction_table WHERE user_Id = :userId")
    suspend fun getTotalExpense(userId: String): Double?

    @Query("SELECT * FROM transaction_table WHERE user_Id = :userId AND category = :category ORDER BY transaction_date DESC")
    suspend fun getTransactionsByCategory(userId: String, category: String): List<Transaction>

    @Query("SELECT * FROM transaction_table WHERE user_Id = :userId AND DATE(transaction_date) = DATE('now', 'localtime') ORDER BY transaction_date DESC")
    suspend fun getTodayTransactions(userId: String): List<Transaction>

    @Query("SELECT * FROM transaction_table WHERE user_Id = :userId AND strftime('%Y-%W', transaction_date) = strftime('%Y-%W', 'now', 'localtime') ORDER BY transaction_date DESC")
    suspend fun getThisWeekTransactions(userId: String): List<Transaction>

    @Query("SELECT * FROM transaction_table WHERE user_Id = :userId AND strftime('%Y-%m', transaction_date) = strftime('%Y-%m', 'now', 'localtime') ORDER BY transaction_date DESC")
    suspend fun getThisMonthTransactions(userId: String): List<Transaction>
}