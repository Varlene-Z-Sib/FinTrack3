package com.example.fintrack3

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SubscriptionPage : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SubscriptionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscription_page)

        recyclerView = findViewById(R.id.subscriptionRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val dummyList = listOf(
            Subscription("Netflix", 159.99, "2025-07-01"),
            Subscription("Spotify", 59.99, "2025-06-15"),
            Subscription("Canva Pro", 199.00, "2025-07-10")
        )

        adapter = SubscriptionAdapter(dummyList)
        recyclerView.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            Toast.makeText(this, "Add new subscription clicked", Toast.LENGTH_SHORT).show()
        }

        // Get the userId passed from MainPage
        val userId = intent.getStringExtra("USER_ID")

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
}
