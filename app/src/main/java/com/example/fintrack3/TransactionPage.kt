package com.example.fintrack3

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class TransactionPage : AppCompatActivity() {

    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var recyclerViewTransactions: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaction_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get current user ID from FirebaseAuth
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            // User not logged in, redirect to login or handle accordingly
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Initialize Firestore
        val firestore = FirebaseFirestore.getInstance()

        // Setup ViewModel passing Firestore and userId
        val viewModelFactory = TransactionViewModelFactory(firestore, userId)
        transactionViewModel = ViewModelProvider(this, viewModelFactory).get(TransactionViewModel::class.java)

        recyclerViewTransactions = findViewById(R.id.recyclerViewTransactions)
        recyclerViewTransactions.layoutManager = LinearLayoutManager(this)

        transactionAdapter = TransactionAdapter()
        recyclerViewTransactions.adapter = transactionAdapter

        // Observe LiveData from ViewModel
        transactionViewModel.transactions.observe(this) { transactions ->
            transactionAdapter.submitList(transactions)
        }

        // Bottom Navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainPage::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // FAB to add new transaction
        val fabAddTransaction = findViewById<ImageView>(R.id.fabAddTransaction)
        fabAddTransaction.setOnClickListener {
            val intent = Intent(this, NewTransaction::class.java)
            startActivity(intent)
        }
    }
}

// ViewModel Factory to inject Firestore and userId
class TransactionViewModelFactory(
    private val firestore: FirebaseFirestore,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(firestore, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
