package com.example.fintrack3

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class BudgetActivity : AppCompatActivity() {

    private lateinit var txtCurrentMonth: TextView
    private lateinit var tvCurrentSpending: TextView
    private lateinit var tvTopCategory: TextView
    private lateinit var tvSavingsGoal: TextView
    private lateinit var tvTechGoal: TextView
    private lateinit var progressSavings: ProgressBar
    private lateinit var progressTech: ProgressBar
    private lateinit var btnPrevMonth: ImageButton
    private lateinit var btnNextMonth: ImageButton
    private lateinit var toggleGroup: MaterialButtonToggleGroup

    private var currentMonth: Calendar = Calendar.getInstance()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        // UI references
        txtCurrentMonth = findViewById(R.id.txtCurrentMonth)
        tvCurrentSpending = findViewById(R.id.tvCurrentSpending)
        tvTopCategory = findViewById(R.id.tvTopCategory)
        tvSavingsGoal = findViewById(R.id.tvSavingsGoal)
        tvTechGoal = findViewById(R.id.tvTechGoal)
        progressSavings = findViewById(R.id.progressSavings)
        progressTech = findViewById(R.id.progressTech)
        btnPrevMonth = findViewById(R.id.btnPrevMonth)
        btnNextMonth = findViewById(R.id.btnNextMonth)
        toggleGroup = findViewById(R.id.btnTogglePeriod)

        updateMonthDisplay()

        // Handle month changes
        btnPrevMonth.setOnClickListener {
            currentMonth.add(Calendar.MONTH, -1)
            updateMonthDisplay()
            fetchBudgetData()
        }

        btnNextMonth.setOnClickListener {
            currentMonth.add(Calendar.MONTH, 1)
            updateMonthDisplay()
            fetchBudgetData()
        }

        toggleGroup.addOnButtonCheckedListener { _, _, _ ->
            fetchBudgetData()
        }

        fetchBudgetData()
    }

    private fun updateMonthDisplay() {
        val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        txtCurrentMonth.text = formatter.format(currentMonth.time)
    }

    private fun fetchBudgetData() {
        val userId = auth.currentUser?.uid ?: return
        val period = when (toggleGroup.checkedButtonId) {
            R.id.btnWeek -> "week"
            R.id.btnMonth -> "month"
            R.id.btnYear -> "year"
            else -> "month"
        }

        val monthKey = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(currentMonth.time)

        firestore.collection("budgets")
            .document(userId)
            .collection(period)
            .document(monthKey)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val savingsUsed = document.getDouble("savingsUsed") ?: 0.0
                    val savingsGoal = document.getDouble("savingsGoal") ?: 1.0
                    val expenseUsed = document.getDouble("expenseUsed") ?: 0.0
                    val expenseLimit = document.getDouble("expenseLimit") ?: 1.0
                    val topCategory = document.getString("topCategory") ?: "No data"

                    val savingsProgress = ((savingsUsed / savingsGoal) * 100).toInt().coerceIn(0, 100)
                    val expenseProgress = ((expenseUsed / expenseLimit) * 100).toInt().coerceIn(0, 100)

                    tvSavingsGoal.text = "goal - R${String.format("%.2f", savingsGoal)}"
                    tvTechGoal.text = "limit - R${String.format("%.2f", expenseLimit)}"
                    progressSavings.progress = savingsProgress
                    progressTech.progress = expenseProgress

                    tvCurrentSpending.text = "Current spending: R${String.format("%.2f", expenseUsed)}"
                    tvTopCategory.text = "Top category: $topCategory"
                } else {
                    resetViews()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load budget data.", Toast.LENGTH_SHORT).show()
                resetViews()
            }
    }

    private fun resetViews() {
        tvSavingsGoal.text = "goal - R0.00"
        tvTechGoal.text = "limit - R0.00"
        progressSavings.progress = 0
        progressTech.progress = 0
        tvCurrentSpending.text = "Current spending: R0.00"
        tvTopCategory.text = "No spending data available"
    }
}
