package com.example.fintrackv20

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fintrackv20.roomDB.FinTrackDB
import com.example.fintrackv20.roomDB.UserDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BudgetActivity : AppCompatActivity() {

    private var userId: String? = null
    private lateinit var userDao: UserDao
    private lateinit var tvTechGoal: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_budget)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get the userId passed from MainPage
        userId = intent.getStringExtra("USER_ID")

        // Initialize database and UserDao
        val db = FinTrackDB.getInstance(applicationContext)
        userDao = db.userDao()

        // Find the TextView for displaying the expense limit
        tvTechGoal = findViewById(R.id.tvTechGoal)

        // Fetch user data and update the expense limit TextView
        userId?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val user = userDao.getUserById(it.toLong())
                withContext(Dispatchers.Main) {
                    user?.let {
                        tvTechGoal.text = String.format("limit - R%.2f", it.monthlymax)
                    }
                }
            }
        }
    }
}