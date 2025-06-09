package com.example.fintrack3.repository

import com.example.fintrack3.models.Category
import com.google.firebase.firestore.FirebaseFirestore

class CategoryRepository {
    private val db = FirebaseFirestore.getInstance()
    private val categoryRef = db.collection("categories")

    fun addCategory(category: Category, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val docRef = categoryRef.document()
        val newCategory = category.copy(categoryId = docRef.id)
        docRef.set(newCategory)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getCategoriesForUser(userId: String, onSuccess: (List<Category>) -> Unit, onFailure: (Exception) -> Unit) {
        categoryRef.whereEqualTo("userId", userId).get()
            .addOnSuccessListener { result ->
                val list = result.mapNotNull { it.toObject(Category::class.java) }
                onSuccess(list)
            }
            .addOnFailureListener { onFailure(it) }
    }
}