package com.example.fintrackv20.roomDB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey(autoGenerate = true) val userId : Long = 0,
    val email: String,
    val password: String,
    @ColumnInfo(name = "user_name") val name: String,
    @ColumnInfo(name = "monthly_min") val monthlymin: Double,
    @ColumnInfo(name = "monthly_max") val monthlymax: Double,
)
