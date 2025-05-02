    package com.example.fintrackv20

    import android.content.Intent
    import android.os.Bundle
    import android.widget.ImageView
    import androidx.activity.enableEdgeToEdge
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.view.ViewCompat
    import androidx.core.view.WindowInsetsCompat
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.ViewModelProvider
    import androidx.recyclerview.widget.LinearLayoutManager
    import androidx.recyclerview.widget.RecyclerView
    import com.example.fintrackv20.roomDB.FinTrackDB
    import com.google.android.material.bottomnavigation.BottomNavigationView

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

            // Get the userId passed from MainPage
            val userId = intent.getStringExtra("USER_ID")

            val database = FinTrackDB.getInstance(applicationContext)
            val viewModelFactory = TransactionViewModelFactory(database, userId)
            transactionViewModel = ViewModelProvider(this, viewModelFactory).get(TransactionViewModel::class.java)

            recyclerViewTransactions = findViewById(R.id.recyclerViewTransactions)
            recyclerViewTransactions.layoutManager = LinearLayoutManager(this)

            // Initialize transactionAdapter BEFORE observing LiveData
            transactionAdapter = TransactionAdapter()
            recyclerViewTransactions.adapter = transactionAdapter

            // Observe the transactions LiveData from the ViewModel
            transactionViewModel.transactions.observe(this) { transactionItems ->
                transactionAdapter.submitList(transactionItems)
            }

            // Code for navigating back to MainPage when nav_home is clicked
            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
            bottomNavigationView.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> {
                        val intent = Intent(this, MainPage::class.java).apply {
                            putExtra("USER_ID", userId)
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        startActivity(intent)
                        true
                    }
                    else -> false
                }
            }

            // Code for navigating to NewTransactionPage when fabAddTransaction is clicked
            val fabAddTransaction = findViewById<ImageView>(R.id.fabAddTransaction)
            fabAddTransaction.setOnClickListener {
                val intent = Intent(this, NewTransaction::class.java)
                intent.putExtra("USER_ID", userId)
                startActivity(intent)
            }
        }
    }

    // ViewModel Factory to pass the userId to the ViewModel
    class TransactionViewModelFactory(
        private val database: FinTrackDB,
        private val userId: String?
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TransactionViewModel(database, userId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }