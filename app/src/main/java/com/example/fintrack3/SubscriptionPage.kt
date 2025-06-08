package com.example.fintrack3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
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
    }
}
