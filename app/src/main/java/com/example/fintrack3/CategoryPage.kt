package com.example.fintrack3

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fintrack3.adapter.CategoryAdapter
import com.example.fintrack3.models.Category
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class CategoryPage : AppCompatActivity() {

    private var userId: String? = null
    private lateinit var expenseRecyclerView: RecyclerView
    private lateinit var expenseCategoryAdapter: CategoryAdapter
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_category_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userId = intent.getStringExtra("USER_ID")
        expenseRecyclerView = findViewById(R.id.recyclerViewExpense)
        expenseRecyclerView.layoutManager = LinearLayoutManager(this)
        expenseCategoryAdapter = CategoryAdapter(emptyList())
        expenseRecyclerView.adapter = expenseCategoryAdapter

        loadUserCategories()

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
        loadUserCategories()
    }

    private fun loadUserCategories() {
        userId?.let { id ->
            firestore.collection("categories")
                .whereEqualTo("userId", id)
                .get()
                .addOnSuccessListener { result ->
                    val categoryList = result.documents.mapNotNull { doc ->
                        doc.toObject(Category::class.java)
                    }
                    expenseCategoryAdapter.updateCategories(categoryList)
                    Log.d("CategoryPage", "Loaded ${categoryList.size} categories for user: $id")
                }
                .addOnFailureListener { e ->
                    Log.e("CategoryPage", "Error loading categories: ${e.message}")
                }
        } ?: Log.e("CategoryPage", "User ID is null")
    }
}
