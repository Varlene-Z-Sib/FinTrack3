package com.example.fintrack3

/*import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log

class MyApp : Application() {

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate() {
        super.onCreate()
        initializeFirestoreWithUsersAndCategories()
    }

    private fun initializeFirestoreWithUsersAndCategories() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val usersRef = firestore.collection("users")
                val categoriesRef = firestore.collection("categories")

                // Check if users collection is empty
                val usersSnapshot = usersRef.get().await()
                if (usersSnapshot.isEmpty) {
                    val user1 = hashMapOf(
                        "email" to "varlene@example.com",
                        "password" to "password123",
                        "name" to "Varlene",
                        "monthlymin" to 3000.0,
                        "monthlymax" to 7000.0
                    )
                    val user2 = hashMapOf(
                        "email" to "mpho@example.com",
                        "password" to "securepass",
                        "name" to "Mpho",
                        "monthlymin" to 2500.0,
                        "monthlymax" to 3000.0
                    )
                    val user3 = hashMapOf(
                        "email" to "siphe@example.com",
                        "password" to "mysecret",
                        "name" to "Siphe",
                        "monthlymin" to 1000.0,
                        "monthlymax" to 2500.0
                    )
                    usersRef.add(user1)
                    usersRef.add(user2)
                    usersRef.add(user3)

                    Log.d("FirestoreInit", "Initial users added to Firestore")
                } else {
                    Log.d("FirestoreInit", "Users collection already has data")
                }

                // Check if categories collection is empty
                val categoriesSnapshot = categoriesRef.get().await()
                if (categoriesSnapshot.isEmpty) {
                    categoriesRef.add(hashMapOf("userId" to "1", "name" to "Groceries"))
                    categoriesRef.add(hashMapOf("userId" to "2", "name" to "Entertainment"))
                    categoriesRef.add(hashMapOf("userId" to "3", "name" to "Food"))
                    categoriesRef.add(hashMapOf("userId" to "1", "name" to "Tech"))
                    categoriesRef.add(hashMapOf("userId" to "2", "name" to "Movies"))
                    categoriesRef.add(hashMapOf("userId" to "3", "name" to "Movies"))

                    Log.d("FirestoreInit", "Initial categories added to Firestore")
                } else {
                    Log.d("FirestoreInit", "Categories collection already has data")
                }

            } catch (e: Exception) {
                Log.e("FirestoreInit", "Error initializing Firestore data", e)
            }
        }
    }
}
*/