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
import com.example.fintrack3.roomDB.FinTrackDB
import com.example.myapplication.room.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Date
import android.os.Build // Import Build

private const val REQUEST_CODE_PICK_IMAGE = 124
private const val REQUEST_CODE_STORAGE_PERMISSION = 457 // Keep this constant for the request code

class NewTransaction : AppCompatActivity() {

    private lateinit var filepathText: TextView
    private var selectedImageUri: Uri? = null
    private lateinit var db: FinTrackDB // Room database instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_transaction)

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
            // It's generally better to store the Uri as a String in the database,
            // or even better, if you only need to display it, you might not need to save the path at all
            val imagePath = selectedImageUri?.toString() // Store the URI string directly

            if (amount != null && dateString.isNotEmpty()) {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val transactionDate = sdf.parse(dateString)

                val currentUserId = userId.toString()

                if (transactionDate != null) {
                    val newTransaction = Transaction(
                        amount = amount,
                        description = description,
                        image = imagePath, // Use the URI string
                        date = Date(transactionDate.time),
                        userId = currentUserId,
                        category = category
                    )

                    CoroutineScope(Dispatchers.IO).launch {
                        db.transactionDao().insert(newTransaction)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@NewTransaction, "Transaction added", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@NewTransaction, TransactionPage::class.java).apply {
                                putExtra("USER_ID", currentUserId)
                            }
                            startActivity(intent)
                            finish()
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
            // Check permissions based on Android version
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 (API 33) and above
                if (checkMediaPermissions()) {
                    openImagePicker()
                } else {
                    requestMediaPermissions()
                }
            } else { // Android 12L (API 32) and below
                if (checkStoragePermission()) {
                    openImagePicker()
                } else {
                    requestStoragePermission()
                }
            }
        }
    }

    // For Android 12L (API 32) and below
    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    // For Android 12L (API 32) and below
    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            REQUEST_CODE_STORAGE_PERMISSION
        )
    }

    // For Android 13 (API 33) and above
    private fun checkMediaPermissions(): Boolean {
        val readImagesPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED

        val readVideoPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_MEDIA_VIDEO
        ) == PackageManager.PERMISSION_GRANTED

        // You might only need READ_MEDIA_IMAGES if you're strictly picking images
        return readImagesPermission && readVideoPermission
    }

    // For Android 13 (API 33) and above
    private fun requestMediaPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO),
            REQUEST_CODE_STORAGE_PERMISSION // Use the same request code for simplicity
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            // Check if any of the requested permissions were granted
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
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
        // It's highly recommended to use the new Photo Picker if targetting Android 13+
        // This avoids needing explicit storage permissions if the user only picks a few photos.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val intent = Intent(MediaStore.ACTION_PICK_IMAGES)
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
        } else {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                selectedImageUri = data.data // Store the URI directly
                selectedImageUri?.let { uri ->
                    filepathText.text = uri.lastPathSegment ?: "No path available" // Display something meaningful
                    Log.d("NewTransactionActivity", "URI: $uri")

                    // If you *must* have the real path (e.g., for direct file operations outside MediaStore),
                    // be aware this might not always work and is less recommended for privacy.
                    // If you only need to display the image, the URI is sufficient.
                    // val filePath = getRealPathFromURI(uri)
                    // filepathText.text = filePath ?: "No path available"
                }
            }
        }
    }

    // Helper function to get the actual file path (less reliable on newer Android versions)
    // Consider removing this if you can work directly with the Uri.
    private fun getRealPathFromURI(contentURI: Uri): String? {
        // For Android 10 (API 29) and above, querying MediaStore.Images.Media.DATA is deprecated and might return null
        // or a different path than expected due to Scoped Storage.
        // It's often better to work directly with the Uri and use content resolvers for file access.
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