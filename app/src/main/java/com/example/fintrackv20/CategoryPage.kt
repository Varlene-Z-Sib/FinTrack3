package com.example.fintrackv20

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fintrackv20.adapter.CategoryAdapter
import com.example.fintrackv20.roomDB.FinTrackDB
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoryPage : AppCompatActivity() {

    private var userId: String? = null
    private lateinit var expenseRecyclerView: RecyclerView
    private lateinit var expenseCategoryAdapter: CategoryAdapter
    private lateinit var categoryDao: FinTrackDB

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
        categoryDao = FinTrackDB.getInstance(applicationContext)
        expenseRecyclerView = findViewById(R.id.recyclerViewExpense)
        expenseRecyclerView.layoutManager = LinearLayoutManager(this)
        expenseCategoryAdapter = CategoryAdapter(emptyList()) // Initialize with an empty list
        expenseRecyclerView.adapter = expenseCategoryAdapter

        loadUserCategories()
        /*val newCategoryButton = findViewById<Button>(R.id.btnNewCategory)
        newCategoryButton.setOnClickListener {
            // Assuming you have a way to get the current user's ID

            val intent = Intent(this, CategoryActivity::class.java)
            intent.putExtra("USER_ID", userId) // Pass the userId as an extra
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
        loadUserCategories() // Reload categories when the activity resumes
    }

    private fun loadUserCategories() {
        userId?.let { id ->
            lifecycleScope.launch(Dispatchers.IO) {
                val categories = categoryDao.categoryDao().getCategoriesByUser(id)
                withContext(Dispatchers.Main) {
                    expenseCategoryAdapter.updateCategories(categories)
                    Log.d("CategoryPage", "Loaded ${categories.size} categories for user: $id")
                }
            }
        } ?: run {
            Log.e("CategoryPage", "User ID is null")
        }
    }

}