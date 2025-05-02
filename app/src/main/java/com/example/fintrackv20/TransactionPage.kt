    package com.example.fintrackv20

    import android.content.Intent
    import android.os.Bundle
    import android.widget.ImageView
    import androidx.activity.enableEdgeToEdge
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.view.ViewCompat
    import androidx.core.view.WindowInsetsCompat
    import com.google.android.material.bottomnavigation.BottomNavigationView

    class TransactionPage : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            setContentView(R.layout.activity_transaction_page)
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }

            // Code for navigating back to MainPage when nav_home is clicked
            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
            bottomNavigationView.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> {
                        val intent = Intent(this, MainPage::class.java)
                        startActivity(intent)
                        true
                    }
                    // You can add other bottom navigation item handling here if needed
                    else -> false
                }
            }

            // Code for navigating to NewTransactionPage when fabAddTransaction is clicked
            val fabAddTransaction = findViewById<ImageView>(R.id.fabAddTransaction)
            fabAddTransaction.setOnClickListener {
                val intent = Intent(this, NewTransaction::class.java)
                startActivity(intent)
            }
        }
    }