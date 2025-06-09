package com.example.fintrack3.repository

import com.example.fintrack3.models.User
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersRef = db.collection("users")

    fun addUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        usersRef.document(user.userId).set(user)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getUser(userId: String, onSuccess: (User?) -> Unit, onFailure: (Exception) -> Unit) {
        usersRef.document(userId).get()
            .addOnSuccessListener { snapshot ->
                onSuccess(snapshot.toObject(User::class.java))
            }
            .addOnFailureListener { onFailure(it) }
    }
}