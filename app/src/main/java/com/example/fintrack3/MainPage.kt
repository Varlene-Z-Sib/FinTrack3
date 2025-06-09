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
    private lateinit var totalIncomeTextView: TextView // Added for total income
    private lateinit var totalBalanceTextView: TextView // Added for total balance

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
        totalIncomeTextView = findViewById(R.id.txtincometotal) // Assuming you'll add this TextView in activity_main_page.xml
        totalBalanceTextView = findViewById(R.id.txtbalancetotal) // Assuming you'll add this TextView in activity_main_page.xml
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
            fetchAndDisplayFinancialSummary(userId!!)
        } else {
            totalExpenseTextView.text = "R 0.00"
            totalIncomeTextView.text = "R 0.00"
            totalBalanceTextView.text = "R 0.00"
        }
    }

    private fun fetchAndDisplayFinancialSummary(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot: QuerySnapshot = firestore.collection("transactions")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                var totalExpenses = 0.0
                var totalIncome = 0.0

                for (document in snapshot.documents) {
                    val transaction = document.toObject(Transaction::class.java)
                    transaction?.let { // Ensure transaction is not null
                        if (it.type == "Expense") {
                            totalExpenses += it.amount
                        } else if (it.type == "Income") {
                            totalIncome += it.amount
                        }
                    }
                }

                val totalBalance = totalIncome - totalExpenses

                withContext(Dispatchers.Main) {
                    totalExpenseTextView.text = "R %.2f".format(totalExpenses)
                    totalIncomeTextView.text = "R %.2f".format(totalIncome)
                    totalBalanceTextView.text = "R %.2f".format(totalBalance)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    totalExpenseTextView.text = "R 0.00"
                    totalIncomeTextView.text = "R 0.00"
                    totalBalanceTextView.text = "R 0.00"
                }
            }
        }
    }
}


