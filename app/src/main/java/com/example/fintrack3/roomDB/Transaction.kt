package com.example.myapplication.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity(tableName = "transaction_table")

data class Transaction(
    @PrimaryKey(autoGenerate = true) val transactionId: Long = 0,
    val amount: Double,
    val description: String,
    @ColumnInfo(name = "image_path") val image: String?,
    @ColumnInfo(name = "transaction_date") val date: Date,
    @ColumnInfo(name = "user_Id") val userId: String,
    val category: String
)