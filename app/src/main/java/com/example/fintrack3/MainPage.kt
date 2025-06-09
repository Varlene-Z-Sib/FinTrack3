package com.example.fintrack3

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainPage : AppCompatActivity() {

    private var userId: String? = null
    private lateinit var totalExpenseTextView: TextView
    private lateinit var transactionDao: TransactionDao
    private lateinit var settingsIcon: ImageView
    private lateinit var viewBudgetCard: LinearLayout

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
        userId = intent.getStringExtra("USER_ID")

        // Initialize database and TransactionDao
        val db = FinTrackDB.getInstance(applicationContext)
        transactionDao = db.transactionDao()

        // Find the TextView for displaying total expenses
        totalExpenseTextView = findViewById(R.id.txtexpensetotal)

        // Initialize the settingsIcon
        settingsIcon = findViewById(R.id.settingsIcon) // Find the ImageView by its ID

        // Set click listener for the settingsIcon
        settingsIcon.setOnClickListener {
            // Create an intent to open CategoryPage
            val intent = Intent(this, CategoryPage::class.java).apply {
                // Pass the userId to the CategoryPage
                putExtra("USER_ID", userId)
            }
            // Start the CategoryPage activity
            startActivity(intent)
        }
/*
        // Set click listener for the viewBudgetCard
        viewBudgetCard.setOnClickListener {
            val intent = Intent(this, BudgetActivity::class.java).apply {
                // Pass the userId to the BudgetActivity
                putExtra("USER_ID", userId)
            }
            // Start the BudgetActivity activity
            startActivity(intent)
        }*/

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
                R.id.nav_home -> {
                    // If nav_home should reload MainPage or perform an action that needs the userId
                    val intent = Intent(this, MainPage::class.java).apply {
                        putExtra("USER_ID", userId)
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    startActivity(intent)
                    true
                }
                R.id.nav_analysis -> {
                    val intent = Intent(this, BudgetActivity::class.java).apply {
                        // Pass the userId to the BudgetActivity
                        putExtra("USER_ID", userId)
                    }
                    // Start the BudgetActivity activity
                    startActivity(intent)
                    true
                }
                else -> false // Let other potential listeners handle the click
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Fetch and display total expenses every time the activity resumes
        if (!userId.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                val totalExpense = transactionDao.getTotalExpense(userId.toString())
                withContext(Dispatchers.Main) {
                    totalExpenseTextView.text = if (totalExpense != null) {
                        "R %.2f".format(totalExpense)
                    } else {
                        "R 0.00"
                    }
                }
            }
        } else {
            totalExpenseTextView.text = "R 0.00"
        }
    }
}