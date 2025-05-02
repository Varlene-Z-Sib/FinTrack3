package com.example.fintrackv20

import android.app.Application
import android.util.Log
import com.example.fintrackv20.roomDB.FinTrackDB
import com.example.fintrackv20.roomDB.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApp: Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("AppStart", "Application onCreate called")
        initializeDatabaseWithUsers()
    }

    private fun initializeDatabaseWithUsers() {
        val db = FinTrackDB.getInstance(applicationContext)
        val userDao = db.userDao()
        //val transactionDao = db.transactionDao()
        //val categoryDao = db.categoryDao()

        CoroutineScope(Dispatchers.IO).launch {
            // Check if the database is already populated to avoid duplicates
            //userDao.getAllUsers().isEmpty()
            val users = if (userDao.getAllUsers().isEmpty()) {
                val user1 = User(
                    email = "varlene@example.com",
                    password = "password123",
                    name = "Varlene",
                    monthlymin = 3000.0,
                    monthlymax = 7000.0
                )
                val user2 = User(
                    email = "mpho@example.com",
                    password = "securepass",
                    name = "Mpho",
                    monthlymin = 2500.0,
                    monthlymax = 3000.0
                )
                val user3 = User(
                    email = "siphe@example.com",
                    password = "mysecret",
                    name = "Siphe",
                    monthlymin = 1000.0,
                    monthlymax = 2500.0
                )

                userDao.addUser(user1)
                userDao.addUser(user2)
                userDao.addUser(user3)

                Log.d("DBInit", "Initial users added to the database")
            } else {
                Log.d("DBInit", "Database already contains users, skipping initial population")
            }
        }
    }
}