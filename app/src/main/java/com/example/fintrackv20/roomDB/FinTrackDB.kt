package com.example.fintrackv20.roomDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.room.Transaction
import com.example.myapplication.room.TransactionDao

@Database(entities = [User :: class,
                        Transaction :: class],
                        version = 1)
@TypeConverters(Converters::class)
abstract class FinTrackDB : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao() : TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: FinTrackDB? = null

        fun getInstance(context: Context):FinTrackDB{
            synchronized(this){
                return INSTANCE?: Room.databaseBuilder(
                    context.applicationContext,
                    FinTrackDB::class.java,
                    "finTrack_db"
                ).build().also {
                    INSTANCE = it
                }
            }
        }
    }

}