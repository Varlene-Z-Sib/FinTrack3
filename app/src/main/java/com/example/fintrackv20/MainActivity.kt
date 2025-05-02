package com.example.fintrackv20

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fintrackv20.roomDB.FinTrackDB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var db: FinTrackDB
    private lateinit var userDao: com.example.fintrackv20.roomDB.UserDao // Use the full path to avoid ambiguity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize UI elements
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)

        // Get database instance and UserDao
        db = FinTrackDB.getInstance(applicationContext)
        userDao = db.userDao()

        // Set click listener for the login button
        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            // Perform login check in a coroutine
            CoroutineScope(Dispatchers.IO).launch {
                val user = userDao.getUserByEmail(email)

                withContext(Dispatchers.Main) {
                    if (user != null && user.password == password) {
                        // Login successful, navigate to MainPage and pass userId
                        Toast.makeText(this@MainActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@MainActivity, MainPage::class.java).apply {
                            putExtra("USER_ID", user.userId.toString())
                        }
                        startActivity(intent)
                        finish() // Optional: Close the login activity
                    } else {
                        // Login failed, display an error message
                        Toast.makeText(this@MainActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}