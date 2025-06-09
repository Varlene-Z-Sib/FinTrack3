package com.example.fintrack3

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.util.*

private const val REQUEST_CODE_PICK_IMAGE = 124
private const val REQUEST_CODE_STORAGE_PERMISSION = 457

// Cloudinary constants - Replace with your info
const val CLOUD_NAME = "dmjju7jvd"
const val UPLOAD_PRESET = "UPLOAD_PRESET"

class NewTransaction : AppCompatActivity() {

    private lateinit var filepathText: TextView
    private var selectedImageUri: Uri? = null

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_transaction)

        filepathText = findViewById(R.id.txt_uploadpath)
        val amountEditText = findViewById<EditText>(R.id.et_amount)
        val categorySpinner = findViewById<Spinner>(R.id.sp_category)
        val dateEditText = findViewById<EditText>(R.id.et_date)
        val descriptionEditText = findViewById<EditText>(R.id.et_description)
        val typeGroup = findViewById<RadioGroup>(R.id.typeGroup)
        val addButton = findViewById<Button>(R.id.btn_add)
        val cancelButton = findViewById<Button>(R.id.btn_cancel)
        val pictureButton = findViewById<Button>(R.id.btn_picture)

        // Set spinner categories (you can dynamically load this from Firestore if you want)
        val categories = listOf("Food", "Tech", "Investment", "Shopping")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        // Date picker
        dateEditText.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                dateEditText.setText(String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear))
            }, year, month, day).show()
        }

        // Picture button click - request permission and pick image
        pictureButton.setOnClickListener {
            if (checkStoragePermission()) {
                openImagePicker()
            } else {
                requestStoragePermission()
            }
        }

        // Add transaction button
        addButton.setOnClickListener {
            val amount = amountEditText.text.toString().toDoubleOrNull()
            val category = categorySpinner.selectedItem.toString()
            val dateString = dateEditText.text.toString()
            val description = descriptionEditText.text.toString()
            val type = if (typeGroup.checkedRadioButtonId == R.id.rb_expense) "Expense" else "Income"
            val imageUrl = filepathText.text.toString().takeIf { it.startsWith("http") }

            if (amount == null || dateString.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = sdf.parse(dateString)
            if (date == null) {
                Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUser = auth.currentUser
            if (currentUser == null) {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Build transaction data map
            val transactionData = hashMapOf(
                "amount" to amount,
                "category" to category,
                "date" to date,
                "description" to description,
                "type" to type,
                "userId" to currentUser.uid,
                "imageUrl" to (imageUrl ?: "") // empty string if no image uploaded
            )

            // Save to Firestore
            firestore.collection("transactions")
                .add(transactionData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Transaction added", Toast.LENGTH_SHORT).show()
                    finish() // Close activity and go back
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to add transaction: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    // Storage permission check
    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            REQUEST_CODE_STORAGE_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker()
            } else {
                Toast.makeText(this, "Storage permission is required to select an image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Open gallery to pick image
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    // Receive image URI and upload to Cloudinary
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedImageUri = uri
                val file = uriToFile(uri)
                uploadImageToCloudinary(file,
                    onSuccess = { imageUrl ->
                        runOnUiThread {
                            filepathText.text = imageUrl
                            Toast.makeText(this, "Image uploaded", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onError = { errorMsg ->
                        runOnUiThread {
                            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    })
            }
        }
    }

    // Convert Uri to File
    private fun uriToFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)
        val file = File(cacheDir, "upload_image")
        inputStream.use { input ->
            file.outputStream().use { output ->
                input?.copyTo(output)
            }
        }
        return file
    }

    // Cloudinary upload function using OkHttp
    private fun uploadImageToCloudinary(
        imageFile: File,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"

        val mediaType = "image/*".toMediaTypeOrNull()
        val fileBody = imageFile.asRequestBody(mediaType)

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", imageFile.name, fileBody)
            .addFormDataPart("upload_preset", UPLOAD_PRESET)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                onError("Upload failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    if (responseData != null) {
                        val json = JSONObject(responseData)
                        val imageUrl = json.getString("secure_url")
                        onSuccess(imageUrl)
                    } else {
                        onError("Empty response from server")
                    }
                } else {
                    onError("Upload error: ${response.message}")
                }
            }
        })
    }
}
