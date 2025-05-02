package com.example.fintrackv20

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fintrackv20.roomDB.FinTrackDB
import kotlinx.coroutines.CoroutineScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_page)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get the userId passed from MainActivity
        val userId = intent.getStringExtra("USER_ID")

        // Initialize database and TransactionDao
        val db = FinTrackDB.getInstance(applicationContext)
        val transactionDao = db.transactionDao()

        // Find the TextView for displaying total expenses
        val totalExpenseTextView: TextView = findViewById(R.id.txtexpensetotal)

        // Fetch and display total expenses
        if (!userId.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                val totalExpense = transactionDao.getTotalExpense(userId)
                withContext(Dispatchers.Main) {
                    totalExpenseTextView.text = if (totalExpense != null) {
                        "R %.2f".format(totalExpense)
                    } else {
                        "R 0.00"
                    }
                }
            }
        } else {
            // Handle the case where userId is not passed (shouldn't happen if login is correct)
            totalExpenseTextView.text = "R 0.00"
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_transactions -> {
                    // Create the intent
                    val intent = Intent(this, TransactionPage::class.java).apply {
                        // Add the USER_ID as an extra to the intent
                        putExtra("USER_ID", userId)
                    }
                    // Start the TransactionPage activity
                    startActivity(intent)
                    true // Indicate that the item click was handled
                }

                else -> false // Let other potential listeners handle the click
            }
        }
    }
}