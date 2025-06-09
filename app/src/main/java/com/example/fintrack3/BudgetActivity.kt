package com.example.fintrack3

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.fintrack3.models.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.*

class BudgetActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var tvCurrentSpending: TextView
    private lateinit var tvTopCategory: TextView
    private lateinit var tvSavingsGoal: TextView
    private lateinit var tvLimit: TextView
    private lateinit var progressSavings: ProgressBar
    private lateinit var progressLimit: ProgressBar
    private lateinit var btnNewBudget: Button
    private lateinit var btnViewGraphs: Button

    // Default budget values
    private var savingsGoal = 3000.0
    private var expenseLimit = 1500.0

    private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA")) // R-format

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Find Views
        tvCurrentSpending = findViewById(R.id.tvCurrentSpending)
        tvTopCategory = findViewById(R.id.tvTopCategory)
        tvSavingsGoal = findViewById(R.id.tvSavingsGoal)
        tvLimit = findViewById(R.id.tvLimit)
        progressSavings = findViewById(R.id.progressSavings)
        progressLimit = findViewById(R.id.progressLimit)

        btnNewBudget = findViewById(R.id.btnNewBudget)
        btnViewGraphs = findViewById(R.id.btnViewGraphs)

        btnNewBudget.setOnClickListener {
            showNewBudgetDialog()
        }

        btnViewGraphs.setOnClickListener {
            val intent = Intent(this, GraphActivity::class.java) // Adjust if your Graph activity class is named differently
            startActivity(intent)
        }

        loadBudgetData()
    }

    private fun loadBudgetData() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("transactions")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                var totalExpenses = 0.0
                var totalIncome = 0.0
                val categoryMap = mutableMapOf<String, Double>()

                for (doc in result) {
                    val transaction = doc.toObject(Transaction::class.java)

                    val amount = transaction.amount
                    if (amount < 0) {
                        // Expense (store positive value for total expenses)
                        totalExpenses += -amount
                        categoryMap[transaction.category] =
                            categoryMap.getOrDefault(transaction.category, 0.0) + -amount
                    } else {
                        // Income
                        totalIncome += amount
                    }
                }

                val savings = totalIncome - totalExpenses
                val savingsPercent = ((savings / savingsGoal) * 100).toInt().coerceIn(0, 100)
                val expensePercent = ((totalExpenses / expenseLimit) * 100).toInt().coerceIn(0, 100)

                // Update progress bars
                progressSavings.progress = savingsPercent
                progressLimit.progress = expensePercent

                // Update text views with formatted currency
                tvSavingsGoal.text = "Goal: ${currencyFormat.format(savingsGoal)}"
                tvLimit.text = "Limit: ${currencyFormat.format(expenseLimit)}"
                tvCurrentSpending.text = "Spent: ${currencyFormat.format(totalExpenses)} | Saved: ${currencyFormat.format(savings)}"

                // Show top spending category
                val topCategory = categoryMap.maxByOrNull { it.value }?.key ?: "N/A"
                tvTopCategory.text = "Top Category: $topCategory"
            }
            .addOnFailureListener {
                tvCurrentSpending.text = "Failed to load data"
                tvTopCategory.text = ""
            }
    }

    private fun showNewBudgetDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_new_budget, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Set New Budget")
            .setPositiveButton("Save") { dialog, _ ->
                val savingsInput = dialogView.findViewById<EditText>(R.id.etGoal)
                val limitInput = dialogView.findViewById<EditText>(R.id.etLimit)

                val newSavingsGoal = savingsInput.text.toString().toDoubleOrNull()
                val newExpenseLimit = limitInput.text.toString().toDoubleOrNull()

                if (newSavingsGoal != null) savingsGoal = newSavingsGoal
                if (newExpenseLimit != null) expenseLimit = newExpenseLimit

                loadBudgetData()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        dialogBuilder.create().show()
    }
}
