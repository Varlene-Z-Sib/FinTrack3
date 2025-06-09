package com.example.fintrack3

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.util.Log
import com.example.myapplication.room.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Date

private const val REQUEST_CODE_PICK_IMAGE = 124
private const val REQUEST_CODE_STORAGE_PERMISSION = 457

class NewTransaction : AppCompatActivity() {

    private lateinit var filepathText: TextView
    private var selectedImageUri: Uri? = null
    private lateinit var db: FinTrackDB // Room database instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_transaction)

        // Get the userId passed from MainPage
        val userId = intent.getStringExtra("USER_ID")

        filepathText = findViewById(R.id.txt_uploadpath)

        val amountEditText = findViewById<EditText>(R.id.et_amount)
        val categorySpinner = findViewById<Spinner>(R.id.sp_category)
        val dateEditText = findViewById<EditText>(R.id.et_date)
        val descriptionEditText = findViewById<EditText>(R.id.et_description)
        val typeGroup = findViewById<RadioGroup>(R.id.typeGroup)
        val addButton = findViewById<Button>(R.id.btn_add)
        val cancelButton = findViewById<Button>(R.id.btn_cancel)
        val pictureButton = findViewById<Button>(R.id.btn_picture)

        // Initialize the FinTrackDB
        db = FinTrackDB.getInstance(applicationContext)

        val categories = listOf("Food", "Tech", "Investment", "Shopping")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        dateEditText.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                dateEditText.setText(String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear))
            }, year, month, day).show()
        }

        addButton.setOnClickListener {
            val type = if (typeGroup.checkedRadioButtonId == R.id.rb_expense) "Expense" else "Income"
            val amount = amountEditText.text.toString().toDoubleOrNull()
            val category = categorySpinner.selectedItem.toString()
            val dateString = dateEditText.text.toString()
            val description = descriptionEditText.text.toString()
            val imagePath = filepathText.text.toString().takeIf { it != "No file chosen" && it.isNotEmpty() }

            if (amount != null && dateString.isNotEmpty()) {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val transactionDate = sdf.parse(dateString)

                // Assuming you have a way to get the current user's ID
                val currentUserId = userId.toString() // Replace with your actual user ID retrieval logic

                if (transactionDate != null) {
                    val newTransaction = Transaction(
                        amount = amount,
                        description = description,
                        image = imagePath,
                        date = Date(transactionDate.time),
                        userId = currentUserId,
                        category = category
                    )

                    // Insert the new transaction into the database using a coroutine
                    CoroutineScope(Dispatchers.IO).launch {
                        db.transactionDao().insert(newTransaction)
                        // Switch back to the main thread for UI updates
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@NewTransaction, "Transaction added", Toast.LENGTH_SHORT).show()
                            // Create an Intent to go back to TransactionPage
                            val intent = Intent(this@NewTransaction, TransactionPage::class.java).apply {
                                // Pass the USER_ID back to TransactionPage
                                putExtra("USER_ID", currentUserId)
                            }
                            startActivity(intent)
                            finish() // Optional: Close NewTransaction activity
                        }
                    }
                } else {
                    Toast.makeText(this@NewTransaction, "Invalid date format", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener {
            finish()
        }


        pictureButton.setOnClickListener {
            if (checkStoragePermission()) {
                openImagePicker()
            } else {
                requestStoragePermission()
            }
        }

    }

    private fun checkStoragePermission(): Boolean {
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        return permission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            REQUEST_CODE_STORAGE_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker()
            } else {
                Toast.makeText(
                    this,
                    "Storage permission is required to select an image",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                selectedImageUri = data.data // Store the URI
                selectedImageUri?.let { uri ->
                    val filePath = getRealPathFromURI(uri) //Use new function
                    filepathText.text = filePath ?: "No path available"
                    Log.d("NewTransactionActivity", "File Path: $filePath, URI: $uri")
                }
            }
        }
    }

    //Helper function to get the actual file path
    private fun getRealPathFromURI(contentURI: Uri): String? {
        val cursor = contentResolver.query(contentURI, null, null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(MediaStore.Images.Media.DATA)
                if (columnIndex >= 0) {
                    it.getString(columnIndex)
                } else {
                    null
                }
            } else {
                null
            }
        }
    }
}
