package com.example.fintrack3

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.example.fintrack3.models.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainPage : AppCompatActivity() {

    private var userId: String? = null
    private lateinit var totalExpenseTextView: TextView
    private lateinit var settingsIcon: ImageView
    // private lateinit var viewBudgetCard: LinearLayout // Uncomment if used

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_page)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userId = intent.getStringExtra("USER_ID")

        totalExpenseTextView = findViewById(R.id.txtexpensetotal)
        settingsIcon = findViewById(R.id.settingsIcon)

        settingsIcon.setOnClickListener {
            val intent = Intent(this, CategoryPage::class.java).apply {
                putExtra("USER_ID", userId)
            }
            startActivity(intent)
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_transactions -> {
                    val intent = Intent(this, TransactionPage::class.java).apply {
                        putExtra("USER_ID", userId)
                    }
                    startActivity(intent)
                    true
                }
                R.id.nav_home -> {
                    val intent = Intent(this, MainPage::class.java).apply {
                        putExtra("USER_ID", userId)
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    startActivity(intent)
                    true
                }
                R.id.nav_analysis -> {
                    val intent = Intent(this, BudgetActivity::class.java).apply {
                        putExtra("USER_ID", userId)
                    }
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!userId.isNullOrEmpty()) {
            fetchAndDisplayTotalExpense(userId!!)
        } else {
            totalExpenseTextView.text = "R 0.00"
        }
    }

    private fun fetchAndDisplayTotalExpense(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Query Firestore for all transactions of this user
                val snapshot: QuerySnapshot = firestore.collection("transactions")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                val transactions: List<Transaction> = snapshot.toObjects(Transaction::class.java)

                // Calculate total expenses (assuming expenses are negative or use category to filter)
                // Adjust this logic if you distinguish between income and expense transactions.
                val totalExpense = transactions.sumOf { it.amount }

                withContext(Dispatchers.Main) {
                    totalExpenseTextView.text = "R %.2f".format(totalExpense)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    totalExpenseTextView.text = "R 0.00"
                }
            }
        }
    }
}
